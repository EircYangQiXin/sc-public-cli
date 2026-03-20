package com.sc.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.system.domain.entity.SysConfig;
import com.sc.system.domain.entity.SysConfigLog;
import com.sc.system.mapper.SysConfigLogMapper;
import com.sc.system.mapper.SysConfigMapper;
import com.sc.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 系统配置参数 Service 实现
 * <p>
 * 设计要点:
 * <ul>
 *   <li>Redis 缓存: 以 {@code sys_config:{configKey}} 为 key 缓存配置值</li>
 *   <li>变更广播: 修改配置后通过 Redis Pub/Sub 通知其他节点刷新缓存</li>
 *   <li>审计日志: 增删改操作记录到 sys_config_log 表，记录真实操作者</li>
 *   <li>事务安全: 缓存操作延迟到事务 afterCommit，避免 DB 回滚后 Redis 不一致</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    private static final String CACHE_KEY_PREFIX = "sys_config:";
    private static final String CACHE_REFRESH_CHANNEL = "sys_config:refresh";

    private final StringRedisTemplate redisTemplate;
    private final SysConfigLogMapper configLogMapper;

    @Override
    public String getConfigValueByKey(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }

        // 1. 先查缓存
        String cachedValue = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + configKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 2. 缓存未命中，查数据库
        SysConfig config = baseMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, configKey)
                        .last("LIMIT 1"));

        if (config != null && config.getConfigValue() != null) {
            // 3. 回写缓存
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + configKey, config.getConfigValue());
            return config.getConfigValue();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertConfig(SysConfig config) {
        baseMapper.insert(config);

        // 审计日志（在事务内，跟随 DB 回滚）
        saveConfigLog(config.getConfigId(), config.getConfigKey(), null, config.getConfigValue(), "INSERT");

        // 缓存操作延迟到事务提交后
        final String key = config.getConfigKey();
        final String value = config.getConfigValue();
        afterCommit(new Runnable() {
            @Override
            public void run() {
                if (StrUtil.isNotBlank(key) && value != null) {
                    redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + key, value);
                }
            }
        });

        log.info("新增系统配置: key={}, value={}", config.getConfigKey(), config.getConfigValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(SysConfig config) {
        // 查询旧值用于审计
        SysConfig oldConfig = baseMapper.selectById(config.getConfigId());
        String oldValue = oldConfig != null ? oldConfig.getConfigValue() : null;
        final String oldKey = oldConfig != null ? oldConfig.getConfigKey() : null;

        baseMapper.updateById(config);

        // 审计日志
        saveConfigLog(config.getConfigId(), config.getConfigKey(), oldValue, config.getConfigValue(), "UPDATE");

        // 缓存操作延迟到事务提交后
        final String newKey = config.getConfigKey();
        final String newValue = config.getConfigValue();
        afterCommit(new Runnable() {
            @Override
            public void run() {
                // 如果 key 发生变化，删除旧 key 缓存
                if (oldKey != null && !oldKey.equals(newKey)) {
                    redisTemplate.delete(CACHE_KEY_PREFIX + oldKey);
                }
                // 更新缓存
                if (StrUtil.isNotBlank(newKey) && newValue != null) {
                    redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + newKey, newValue);
                }
                // 广播变更通知
                redisTemplate.convertAndSend(CACHE_REFRESH_CHANNEL, newKey);
            }
        });

        log.info("修改系统配置: key={}, oldValue={}, newValue={}", config.getConfigKey(), oldValue, config.getConfigValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByIds(Long[] configIds) {
        final List<SysConfig> configs = baseMapper.selectBatchIds(Arrays.asList(configIds));

        // 审计日志
        for (SysConfig config : configs) {
            saveConfigLog(config.getConfigId(), config.getConfigKey(), config.getConfigValue(), null, "DELETE");
        }

        baseMapper.deleteBatchIds(Arrays.asList(configIds));

        // 缓存操作延迟到事务提交后
        afterCommit(new Runnable() {
            @Override
            public void run() {
                for (SysConfig config : configs) {
                    if (StrUtil.isNotBlank(config.getConfigKey())) {
                        redisTemplate.delete(CACHE_KEY_PREFIX + config.getConfigKey());
                    }
                }
            }
        });

        log.info("删除系统配置: ids={}", Arrays.toString(configIds));
    }

    @Override
    public void refreshCache() {
        clearCache();
        List<SysConfig> configs = baseMapper.selectList(null);
        for (SysConfig config : configs) {
            if (StrUtil.isNotBlank(config.getConfigKey()) && config.getConfigValue() != null) {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + config.getConfigKey(), config.getConfigValue());
            }
        }
        log.info("系统配置缓存刷新完成, 共加载 {} 条配置", configs.size());
    }

    @Override
    public void clearCache() {
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("系统配置缓存已清除");
    }

    /**
     * 记录配置变更审计日志
     */
    private void saveConfigLog(Long configId, String configKey, String oldValue, String newValue, String operType) {
        try {
            SysConfigLog configLog = new SysConfigLog();
            configLog.setConfigId(configId);
            configLog.setConfigKey(configKey);
            configLog.setOldValue(oldValue != null ? oldValue : "");
            configLog.setNewValue(newValue != null ? newValue : "");
            configLog.setOperType(operType);
            // 从请求上下文获取真实操作者
            String username = SecurityContextHolder.getUsername();
            configLog.setOperBy(StrUtil.isNotBlank(username) ? username : "system");
            configLog.setOperTime(LocalDateTime.now());
            configLogMapper.insert(configLog);
        } catch (Exception e) {
            log.error("记录配置变更审计日志失败", e);
        }
    }

    /**
     * 注册事务提交后回调，确保缓存操作在 DB 事务成功后才执行
     */
    private void afterCommit(final Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (Exception e) {
                        log.error("事务提交后缓存操作失败", e);
                    }
                }
            });
        } else {
            // 无事务上下文，直接执行
            action.run();
        }
    }
}

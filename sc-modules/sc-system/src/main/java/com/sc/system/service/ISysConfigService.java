package com.sc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.system.domain.entity.SysConfig;

/**
 * 系统配置参数 Service 接口
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 根据参数键名获取参数值（优先从缓存读取）
     *
     * @param configKey 参数键名
     * @return 参数值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 新增配置（同步缓存 + 审计日志）
     *
     * @param config 配置实体
     */
    void insertConfig(SysConfig config);

    /**
     * 修改配置（同步缓存 + 审计日志 + 变更广播）
     *
     * @param config 配置实体
     */
    void updateConfig(SysConfig config);

    /**
     * 删除配置（同步缓存 + 审计日志）
     *
     * @param configIds 配置ID数组
     */
    void deleteConfigByIds(Long[] configIds);

    /**
     * 刷新全量配置缓存
     */
    void refreshCache();

    /**
     * 清除配置缓存
     */
    void clearCache();
}

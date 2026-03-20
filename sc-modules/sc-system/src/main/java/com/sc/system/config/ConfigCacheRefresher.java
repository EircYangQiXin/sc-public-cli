package com.sc.system.config;

import com.sc.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 系统配置缓存刷新器
 * <p>
 * 功能:
 * <ul>
 *   <li>应用启动时预热全量配置缓存</li>
 *   <li>通过 Redis Pub/Sub 监听配置变更通知，刷新本机缓存</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigCacheRefresher {

    private static final String CACHE_REFRESH_CHANNEL = "sys_config:refresh";

    private final ISysConfigService configService;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    /**
     * 应用启动后预热配置缓存
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动 → 预热系统配置缓存...");
        configService.refreshCache();
    }

    /**
     * 注册 Redis Pub/Sub 监听器
     */
    @PostConstruct
    public void registerRefreshListener() {
        redisMessageListenerContainer.addMessageListener(
                new MessageListenerAdapter(new ConfigChangeListener()),
                new ChannelTopic(CACHE_REFRESH_CHANNEL));
        log.info("系统配置变更监听器已注册, channel={}", CACHE_REFRESH_CHANNEL);
    }

    /**
     * 配置变更消息监听器
     */
    private class ConfigChangeListener implements MessageListener {
        @Override
        public void onMessage(Message message, byte[] pattern) {
            String configKey = new String(message.getBody());
            log.info("收到配置变更通知, 刷新缓存: key={}", configKey);
            // 单个 key 刷新: 从 DB 重新加载
            configService.getConfigValueByKey(configKey);
        }
    }
}

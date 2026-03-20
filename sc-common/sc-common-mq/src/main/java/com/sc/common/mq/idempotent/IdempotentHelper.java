package com.sc.common.mq.idempotent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * MQ 消费幂等工具
 * <p>
 * 基于 Redis SETNX 实现消息幂等消费（Exactly-Once 语义），防止重复消费。
 * </p>
 * <p>
 * 使用方式:
 * <pre>
 * &#64;RabbitListener(queues = "xxx")
 * public void consume(Message msg) {
 *     String msgId = msg.getMessageProperties().getMessageId();
 *     if (!idempotentHelper.tryConsume(msgId)) {
 *         return; // 重复消息，跳过
 *     }
 *     try {
 *         // 业务处理...
 *         idempotentHelper.ackConsume(msgId);
 *     } catch (Exception e) {
 *         idempotentHelper.failConsume(msgId);
 *         throw e;
 *     }
 * }
 * </pre>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentHelper {

    private static final String IDEMPOTENT_KEY_PREFIX = "mq:idempotent:";
    /** 幂等锁有效期（秒），防止处理中断后永久锁住 */
    private static final long LOCK_EXPIRE_SECONDS = 300;
    /** 消费完成后记录保留时间（秒），防止消息回放重复消费 */
    private static final long ACK_EXPIRE_SECONDS = 86400;

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_CONSUMED = "CONSUMED";

    private final StringRedisTemplate redisTemplate;

    /**
     * 尝试消费（SETNX 抢锁）
     *
     * @param messageId 消息唯一ID
     * @return true=可以消费, false=重复消息需跳过
     */
    public boolean tryConsume(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            log.warn("消息ID为空，跳过幂等检查");
            return true;
        }

        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        String existing = redisTemplate.opsForValue().get(key);

        // 已消费完成
        if (STATUS_CONSUMED.equals(existing)) {
            log.debug("消息已消费, 跳过: messageId={}", messageId);
            return false;
        }

        // 尝试 SETNX 抢锁
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, STATUS_PROCESSING, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

        if (success == null || !success) {
            log.debug("消息正在消费中, 跳过: messageId={}", messageId);
            return false;
        }

        return true;
    }

    /**
     * 消费成功确认
     *
     * @param messageId 消息唯一ID
     */
    public void ackConsume(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        redisTemplate.opsForValue().set(key, STATUS_CONSUMED, ACK_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 消费失败释放锁（允许重新消费）
     *
     * @param messageId 消息唯一ID
     */
    public void failConsume(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        redisTemplate.delete(key);
    }
}

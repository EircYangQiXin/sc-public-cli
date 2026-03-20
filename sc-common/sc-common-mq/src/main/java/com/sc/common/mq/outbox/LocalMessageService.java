package com.sc.common.mq.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表服务 (Outbox 模式)
 * <p>
 * 可靠消息投递闭环:
 * <ol>
 *   <li>业务方在同一事务中调用 {@link #saveMessage} 保存消息到本地消息表（PENDING）</li>
 *   <li>定时任务调用 {@link #sendPendingMessages}，通过 CAS 原子领取后发送</li>
 *   <li>发送时携带 {@link CorrelationData}（id = 消息表主键），用于回调关联</li>
 *   <li>confirmCallback 内 ack + 无 returned → CONFIRMED</li>
 *   <li>confirmCallback 内 nack 或有 returned → failMessage → PENDING/FAILED</li>
 *   <li>SENT 超过 60s 未收到 confirm → 回退 PENDING 重试</li>
 * </ol>
 * </p>
 * <p>
 * CAS 保护: {@link #doSend} 在发送前先通过
 * {@code UPDATE ... SET status='SENDING' WHERE id=? AND status IN ('PENDING','FAILED')}
 * 原子领取，避免多实例并发重复发送。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalMessageService {

    private static final int DEFAULT_MAX_RETRY = 5;
    private static final long SENT_TIMEOUT_SECONDS = 60;

    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 保存消息到本地消息表（应在业务事务内调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(String messageKey, String exchange, String routingKey, String body) {
        String sql = "INSERT INTO mq_local_message (message_key, exchange, routing_key, message_body, status, retry_count, max_retry, next_retry_time, create_time, update_time) " +
                "VALUES (?, ?, ?, ?, 'PENDING', 0, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, messageKey, exchange, routingKey, body, DEFAULT_MAX_RETRY, now, now, now);
        log.debug("本地消息保存成功: key={}, exchange={}, routingKey={}", messageKey, exchange, routingKey);
    }

    /**
     * 发送待发送/失败/SENT超时的消息（由定时任务周期调用）
     */
    public void sendPendingMessages() {
        recoverStuckSentMessages();

        List<LocalMessage> messages = jdbcTemplate.query(
                "SELECT * FROM mq_local_message WHERE status IN ('PENDING', 'FAILED') AND next_retry_time <= ? AND retry_count < max_retry ORDER BY create_time LIMIT 100",
                new BeanPropertyRowMapper<LocalMessage>(LocalMessage.class),
                LocalDateTime.now());

        for (LocalMessage msg : messages) {
            doSend(msg);
        }
    }

    /**
     * confirm 回调: Broker 确认消息已入队（ack=true 且无 returned）
     */
    public void confirmMessage(Long messageId) {
        updateStatus(messageId, "CONFIRMED", null);
        log.debug("本地消息已确认: id={}", messageId);
    }

    /**
     * confirm nack / return 回调: 消息投递失败
     */
    public void failMessage(Long messageId, String reason) {
        int retryCount = getRetryCount(messageId);
        int maxRetry = getMaxRetry(messageId);
        int newRetryCount = retryCount + 1;
        if (newRetryCount >= maxRetry) {
            updateStatusWithRetry(messageId, "FAILED", reason, newRetryCount, null);
            log.error("本地消息投递失败(已达最大重试): id={}, retryCount={}/{}", messageId, newRetryCount, maxRetry);
        } else {
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds((long) Math.pow(2, newRetryCount) * 10);
            updateStatusWithRetry(messageId, "PENDING", reason, newRetryCount, nextRetry);
            log.warn("本地消息投递失败(将重试): id={}, retryCount={}/{}", messageId, newRetryCount, maxRetry);
        }
    }

    /**
     * 发送单条消息 — CAS 原子领取 + CorrelationData 关联
     * <p>
     * 通过 {@code UPDATE ... SET status='SENDING' WHERE id=? AND status IN ('PENDING','FAILED')}
     * 实现乐观锁，返回 affected==0 则说明已被其他实例领取，直接跳过。
     * </p>
     */
    private void doSend(LocalMessage msg) {
        // CAS 原子领取: 只有成功将 PENDING/FAILED → SENDING 的实例才执行发送
        int affected = jdbcTemplate.update(
                "UPDATE mq_local_message SET status = 'SENDING', update_time = ? WHERE id = ? AND status IN ('PENDING', 'FAILED')",
                LocalDateTime.now(), msg.getId());

        if (affected == 0) {
            // 已被其他实例领取，跳过
            log.debug("消息已被其他实例领取, 跳过: id={}", msg.getId());
            return;
        }

        try {
            CorrelationData correlationData = new CorrelationData(String.valueOf(msg.getId()));

            rabbitTemplate.convertAndSend(msg.getExchange(), msg.getRoutingKey(),
                    msg.getMessageBody(), correlationData);

            // CAS: 仅当状态仍为 SENDING 时才写 SENT，防止 confirm 回调已先行设置 CONFIRMED/FAILED 后被覆盖
            jdbcTemplate.update(
                    "UPDATE mq_local_message SET status = 'SENT', update_time = ? WHERE id = ? AND status = 'SENDING'",
                    LocalDateTime.now(), msg.getId());
            log.debug("本地消息已投递: id={}, key={}", msg.getId(), msg.getMessageKey());
        } catch (Exception e) {
            // 发送异常（网络、序列化等），直接标记以便重试
            int retryCount = msg.getRetryCount() + 1;
            if (retryCount >= msg.getMaxRetry()) {
                updateStatusWithRetry(msg.getId(), "FAILED", e.getMessage(), retryCount, null);
                log.error("本地消息发送异常(已达最大重试): id={}, key={}", msg.getId(), msg.getMessageKey(), e);
            } else {
                LocalDateTime nextRetry = LocalDateTime.now().plusSeconds((long) Math.pow(2, retryCount) * 10);
                updateStatusWithRetry(msg.getId(), "PENDING", e.getMessage(), retryCount, nextRetry);
                log.warn("本地消息发送异常(将重试): id={}, key={}, retryCount={}", msg.getId(), msg.getMessageKey(), retryCount);
            }
        }
    }

    /**
     * 回收卡在 SENT/SENDING 超时的消息
     */
    private void recoverStuckSentMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(SENT_TIMEOUT_SECONDS);
        int affected = jdbcTemplate.update(
                "UPDATE mq_local_message SET status = 'PENDING', error_msg = 'SENT/SENDING 超时回退重试', update_time = ? " +
                        "WHERE status IN ('SENT', 'SENDING') AND update_time < ? AND retry_count < max_retry",
                LocalDateTime.now(), threshold);
        if (affected > 0) {
            log.warn("回收超时消息 {} 条", affected);
        }
    }

    private int getRetryCount(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM mq_local_message WHERE id = ?", Integer.class, id);
        return count != null ? count : 0;
    }

    private int getMaxRetry(Long id) {
        Integer max = jdbcTemplate.queryForObject(
                "SELECT max_retry FROM mq_local_message WHERE id = ?", Integer.class, id);
        return max != null ? max : DEFAULT_MAX_RETRY;
    }

    private void updateStatus(Long id, String status, String errorMsg) {
        jdbcTemplate.update(
                "UPDATE mq_local_message SET status = ?, error_msg = ?, update_time = ? WHERE id = ?",
                status, errorMsg, LocalDateTime.now(), id);
    }

    private void updateStatusWithRetry(Long id, String status, String errorMsg, int retryCount, LocalDateTime nextRetryTime) {
        jdbcTemplate.update(
                "UPDATE mq_local_message SET status = ?, error_msg = ?, retry_count = ?, next_retry_time = ?, update_time = ? WHERE id = ?",
                status, errorMsg, retryCount, nextRetryTime, LocalDateTime.now(), id);
    }
}

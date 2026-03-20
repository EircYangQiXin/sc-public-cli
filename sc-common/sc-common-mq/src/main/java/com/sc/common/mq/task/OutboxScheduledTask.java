package com.sc.common.mq.task;

import com.sc.common.mq.outbox.LocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox 消息投递定时任务
 * <p>
 * 通过 {@code sc.mq.outbox.scheduled-enabled=false} 可关闭此 @Scheduled 定时任务，
 * 改用 XXL-JOB 或其他外部调度器调用 {@link LocalMessageService#sendPendingMessages()}。
 * </p>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnBean(LocalMessageService.class)
@ConditionalOnProperty(name = "sc.mq.outbox.scheduled-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxScheduledTask {

    private final LocalMessageService localMessageService;

    /**
     * 每 10 秒扫描一次待发送消息
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void sendPendingMessages() {
        try {
            localMessageService.sendPendingMessages();
        } catch (Exception e) {
            log.error("Outbox 定时发送任务异常", e);
        }
    }
}

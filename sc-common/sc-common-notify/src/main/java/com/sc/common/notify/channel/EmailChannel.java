package com.sc.common.notify.channel;

import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件通知渠道
 * <p>
 * 仅在 Spring 容器中存在 {@link JavaMailSender} Bean 时生效。
 * 需要引入 spring-boot-starter-mail 并正确配置 SMTP 连接。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(JavaMailSender.class)
public class EmailChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public NotifyResult send(NotifyRequest request) {
        try {
            for (String receiver : request.getReceivers()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(receiver);
                message.setSubject(request.getTitle());
                message.setText(request.getContent());
                mailSender.send(message);
                log.debug("邮件发送成功 → to={}", receiver);
            }
            return NotifyResult.ok();
        } catch (Exception e) {
            log.error("邮件发送失败, receivers={}", request.getReceivers(), e);
            return NotifyResult.fail("邮件发送失败: " + e.getMessage());
        }
    }
}

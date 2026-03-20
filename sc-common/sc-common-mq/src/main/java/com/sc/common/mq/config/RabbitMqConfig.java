package com.sc.common.mq.config;

import com.sc.common.mq.outbox.LocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 核心配置:
 * <ul>
 *   <li>JSON 消息序列化</li>
 *   <li>confirmCallback: 判断 returnedMessage 是否存在，区分「已入队」和「到 Broker 但未入队」</li>
 *   <li>returnCallback: 将路由失败信息存入 CorrelationData.returnedMessage，供 confirm 回调读取</li>
 *   <li>消费者手动 ACK + 预取限制</li>
 * </ul>
 * <p>
 * RabbitMQ 回调时序: returnCallback 先于 confirmCallback 触发。
 * 因此在 confirmCallback 中可以通过 {@code correlationData.getReturned()} 判断消息是否路由成功。
 * </p>
 */
@Slf4j
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     * <p>
     * 关键策略: confirm(ack=true) 并不意味着消息到达了队列，只表示 Broker 接收了消息。
     * 如果 mandatory=true 且消息无法路由到任何队列，returnCallback 会先触发，
     * 并将 ReturnedMessage 存入 CorrelationData。
     * 因此在 confirmCallback 中必须检查 returned 是否为 null 来判断是否真正入队。
     * </p>
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         final ObjectProvider<LocalMessageService> localMessageServiceProvider) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);

        // returnCallback 先于 confirmCallback 触发，
        // Spring AMQP 2.1+ 自动将 ReturnedMessage 关联到 CorrelationData.returned
        template.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                log.error("MQ 消息路由失败: exchange={}, routingKey={}, replyCode={}, replyText={}",
                        returned.getExchange(),
                        returned.getRoutingKey(),
                        returned.getReplyCode(),
                        returned.getReplyText());
                // ReturnedMessage 会自动设置到 CorrelationData.returned，
                // confirmCallback 中统一判断
            }
        });

        // confirmCallback — 统一在此处决定消息最终状态
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (correlationData == null || correlationData.getId() == null) {
                    return;
                }
                Long messageId = parseMessageId(correlationData.getId());
                if (messageId == null) {
                    return;
                }

                LocalMessageService svc = localMessageServiceProvider.getIfAvailable();
                if (svc == null) {
                    return;
                }

                if (!ack) {
                    // Broker 拒绝 (nack)
                    svc.failMessage(messageId, "Broker nack: " + cause);
                    return;
                }

                // ack=true 但消息被 return（到达 Exchange 但未路由到任何 Queue）
                ReturnedMessage returned = correlationData.getReturned();
                if (returned != null) {
                    svc.failMessage(messageId,
                            "消息路由失败: replyCode=" + returned.getReplyCode()
                                    + ", replyText=" + returned.getReplyText());
                    return;
                }

                // ack=true 且未被 return → 消息确认入队
                svc.confirmMessage(messageId);
            }
        });

        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    private static Long parseMessageId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

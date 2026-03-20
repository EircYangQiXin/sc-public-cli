package com.sc.common.mq.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送工具类
 * <p>
 * 封装常用的消息发送模式，统一日志记录和异常处理。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqHelper {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换机
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体（自动序列化为 JSON）
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("MQ 消息发送成功 → exchange={}, routingKey={}", exchange, routingKey);
        } catch (Exception e) {
            log.error("MQ 消息发送失败 → exchange={}, routingKey={}, error={}",
                    exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送消息到默认交换机（使用 routingKey 作为队列名）
     *
     * @param queueName 队列名称
     * @param message   消息体
     */
    public void sendToQueue(String queueName, Object message) {
        try {
            rabbitTemplate.convertAndSend(queueName, message);
            log.debug("MQ 消息发送成功 → queue={}", queueName);
        } catch (Exception e) {
            log.error("MQ 消息发送失败 → queue={}, error={}", queueName, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送延迟消息
     * <p>
     * 需要 RabbitMQ 安装 rabbitmq_delayed_message_exchange 插件，
     * 或通过 TTL + 死信队列实现。
     * </p>
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体
     * @param delayMs    延迟毫秒数
     */
    public void sendDelay(String exchange, String routingKey, Object message, long delayMs) {
        try {
            MessagePostProcessor processor = msg -> {
                msg.getMessageProperties().setDelay((int) delayMs);
                return msg;
            };
            rabbitTemplate.convertAndSend(exchange, routingKey, message, processor);
            log.debug("MQ 延迟消息发送成功 → exchange={}, routingKey={}, delay={}ms",
                    exchange, routingKey, delayMs);
        } catch (Exception e) {
            log.error("MQ 延迟消息发送失败 → exchange={}, routingKey={}, error={}",
                    exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    /**
     * 发送带 TTL 的消息（通过消息过期实现延迟）
     * <p>
     * 不依赖 delayed_message_exchange 插件，
     * 配合死信队列使用。
     * </p>
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体
     * @param ttlMs      消息过期时间（毫秒）
     */
    public void sendWithTtl(String exchange, String routingKey, Object message, long ttlMs) {
        try {
            MessagePostProcessor processor = msg -> {
                msg.getMessageProperties().setExpiration(String.valueOf(ttlMs));
                return msg;
            };
            rabbitTemplate.convertAndSend(exchange, routingKey, message, processor);
            log.debug("MQ TTL 消息发送成功 → exchange={}, routingKey={}, ttl={}ms",
                    exchange, routingKey, ttlMs);
        } catch (Exception e) {
            log.error("MQ TTL 消息发送失败 → exchange={}, routingKey={}, error={}",
                    exchange, routingKey, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}

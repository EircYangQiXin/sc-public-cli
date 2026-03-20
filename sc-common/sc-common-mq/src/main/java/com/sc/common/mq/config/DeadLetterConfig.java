package com.sc.common.mq.config;

import com.sc.common.mq.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 死信队列配置
 * <p>
 * 业务队列消息消费失败/过期后，自动进入死信队列，可用于：
 * <ul>
 *   <li>延迟消息（TTL + DLX）</li>
 *   <li>消费失败重试后的兜底处理</li>
 *   <li>人工排查和重新投递</li>
 * </ul>
 * </p>
 */
@Configuration
public class DeadLetterConfig {

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_DEAD_LETTER, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_DEAD_LETTER).build();
    }

    /**
     * 死信队列绑定到死信交换机
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dead-letter");
    }

    /**
     * 示例：带死信策略的操作日志队列
     * <p>消费失败后消息自动转发到死信交换机</p>
     */
    @Bean
    public Queue operLogQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_OPER_LOG)
                .deadLetterExchange(MqConstants.EXCHANGE_DEAD_LETTER)
                .deadLetterRoutingKey("dead-letter")
                .ttl(60000)  // 消息 TTL 60 秒
                .maxLength(10000) // 最大队列长度
                .build();
    }

    /**
     * 业务直连交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_DIRECT, true, false);
    }

    /**
     * 操作日志队列绑定到业务交换机
     */
    @Bean
    public Binding operLogBinding(Queue operLogQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(operLogQueue).to(directExchange).with(MqConstants.ROUTING_OPER_LOG);
    }
}

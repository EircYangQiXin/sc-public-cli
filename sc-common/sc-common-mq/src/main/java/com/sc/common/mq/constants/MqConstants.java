package com.sc.common.mq.constants;

/**
 * MQ 常量定义
 * <p>
 * 统一管理交换机/队列/路由键名称，避免硬编码。
 * 业务模块可继承此类或定义各自的常量类。
 * </p>
 */
public final class MqConstants {

    private MqConstants() {}

    // ==================== 交换机 ====================

    /** 默认直连交换机 */
    public static final String EXCHANGE_DIRECT = "sc.exchange.direct";

    /** 默认主题交换机 */
    public static final String EXCHANGE_TOPIC = "sc.exchange.topic";

    /** 延迟消息交换机（需安装 rabbitmq_delayed_message_exchange 插件） */
    public static final String EXCHANGE_DELAYED = "sc.exchange.delayed";

    /** 死信交换机 */
    public static final String EXCHANGE_DEAD_LETTER = "sc.exchange.dead-letter";

    // ==================== 队列 ====================

    /** 操作日志队列（示例） */
    public static final String QUEUE_OPER_LOG = "sc.queue.oper-log";

    /** 邮件发送队列（示例） */
    public static final String QUEUE_MAIL = "sc.queue.mail";

    /** 短信发送队列（示例） */
    public static final String QUEUE_SMS = "sc.queue.sms";

    /** 死信队列 */
    public static final String QUEUE_DEAD_LETTER = "sc.queue.dead-letter";

    // ==================== 路由键 ====================

    /** 操作日志路由键 */
    public static final String ROUTING_OPER_LOG = "oper.log";

    /** 邮件路由键 */
    public static final String ROUTING_MAIL = "mail.send";

    /** 短信路由键 */
    public static final String ROUTING_SMS = "sms.send";
}

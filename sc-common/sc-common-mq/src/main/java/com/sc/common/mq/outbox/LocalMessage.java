package com.sc.common.mq.outbox;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 本地消息表实体 (Outbox 模式)
 * <p>
 * 实现可靠消息最终一致性：
 * <ol>
 *   <li>业务操作和消息写入在同一事务内完成</li>
 *   <li>定时任务扫描 PENDING/FAILED 消息重新发送</li>
 *   <li>confirmCallback 成功后标记 CONFIRMED</li>
 * </ol>
 * 状态流转: PENDING → SENT → CONFIRMED / FAILED
 * </p>
 */
@Data
public class LocalMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 消息唯一业务标识 (用于幂等) */
    private String messageKey;

    /** 交换机 */
    private String exchange;

    /** 路由键 */
    private String routingKey;

    /** 消息体 (JSON) */
    private String messageBody;

    /** 消息状态: PENDING/SENT/CONFIRMED/FAILED */
    private String status;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 失败原因 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

package com.sc.common.log.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 操作日志事件（用于异步处理）
 */
@Data
public class OperationLogEvent {

    /** 模块标题 */
    private String title;

    /** 业务类型 */
    private Integer businessType;

    /** 操作人类型 */
    private Integer operatorType;

    /** 方法名 */
    private String method;

    /** 请求URL */
    private String operUrl;

    /** 操作人 */
    private String operName;

    /** 请求参数 */
    private String operParam;

    /** 返回结果 */
    private String jsonResult;

    /** 操作状态 (0正常 1异常) */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** 耗时 (毫秒) */
    private Long costTime;
}

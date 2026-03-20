package com.sc.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * XXL-JOB 执行日志 VO
 */
@Data
@ApiModel(description = "任务执行日志")
public class XxlJobLogVO {

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("执行器主键ID")
    private Integer jobGroup;

    @ApiModelProperty("任务主键ID")
    private Integer jobId;

    @ApiModelProperty("执行器地址")
    private String executorAddress;

    @ApiModelProperty("执行器Handler")
    private String executorHandler;

    @ApiModelProperty("执行器参数")
    private String executorParam;

    @ApiModelProperty("调度时间")
    private Date triggerTime;

    @ApiModelProperty("调度结果码")
    private Integer triggerCode;

    @ApiModelProperty("调度结果信息")
    private String triggerMsg;

    @ApiModelProperty("执行时间")
    private Date handleTime;

    @ApiModelProperty("执行结果码")
    private Integer handleCode;

    @ApiModelProperty("执行结果信息")
    private String handleMsg;
}

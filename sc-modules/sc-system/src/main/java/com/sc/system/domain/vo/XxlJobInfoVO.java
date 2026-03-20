package com.sc.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * XXL-JOB 任务信息 VO
 */
@Data
@ApiModel(description = "定时任务信息")
public class XxlJobInfoVO {

    @ApiModelProperty("任务ID")
    private Integer id;

    @ApiModelProperty("执行器主键ID")
    private Integer jobGroup;

    @ApiModelProperty("任务描述")
    private String jobDesc;

    @ApiModelProperty("负责人")
    private String author;

    @ApiModelProperty("报警邮件")
    private String alarmEmail;

    @ApiModelProperty("调度类型")
    private String scheduleType;

    @ApiModelProperty("调度配置（CRON 表达式等）")
    private String scheduleConf;

    @ApiModelProperty("运行模式")
    private String glueType;

    @ApiModelProperty("JobHandler名称")
    private String executorHandler;

    @ApiModelProperty("执行器参数")
    private String executorParam;

    @ApiModelProperty("路由策略")
    private String executorRouteStrategy;

    @ApiModelProperty("阻塞处理策略")
    private String executorBlockStrategy;

    @ApiModelProperty("任务超时时间（秒）")
    private Integer executorTimeout;

    @ApiModelProperty("失败重试次数")
    private Integer executorFailRetryCount;

    @ApiModelProperty("调度状态：0=停止，1=运行")
    private Integer triggerStatus;

    @ApiModelProperty("上次调度时间")
    private Long triggerLastTime;

    @ApiModelProperty("下次调度时间")
    private Long triggerNextTime;
}

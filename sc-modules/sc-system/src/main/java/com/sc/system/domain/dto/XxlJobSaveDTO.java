package com.sc.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建/编辑任务请求 DTO
 */
@Data
@ApiModel(description = "定时任务创建/编辑请求")
public class XxlJobSaveDTO {

    @ApiModelProperty(value = "任务ID（编辑时必填）")
    private Integer id;

    @ApiModelProperty(value = "执行器主键ID", required = true, example = "1")
    private Integer jobGroup;

    @ApiModelProperty(value = "任务描述", required = true, example = "示例任务")
    private String jobDesc;

    @ApiModelProperty(value = "负责人", required = true, example = "admin")
    private String author;

    @ApiModelProperty(value = "报警邮件")
    private String alarmEmail;

    @ApiModelProperty(value = "调度类型", required = true, example = "CRON")
    private String scheduleType;

    @ApiModelProperty(value = "调度配置", required = true, example = "0 0/5 * * * ?")
    private String scheduleConf;

    @ApiModelProperty(value = "运行模式", required = true, example = "BEAN")
    private String glueType;

    @ApiModelProperty(value = "JobHandler名称", required = true, example = "demoJobHandler")
    private String executorHandler;

    @ApiModelProperty(value = "执行器参数")
    private String executorParam;

    @ApiModelProperty(value = "路由策略", example = "FIRST")
    private String executorRouteStrategy;

    @ApiModelProperty(value = "阻塞处理策略", example = "SERIAL_EXECUTION")
    private String executorBlockStrategy;

    @ApiModelProperty(value = "任务超时时间（秒）", example = "0")
    private Integer executorTimeout;

    @ApiModelProperty(value = "失败重试次数", example = "0")
    private Integer executorFailRetryCount;
}

package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置变更审计日志
 */
@Data
@TableName("sys_config_log")
@ApiModel(description = "系统配置变更审计日志")
public class SysConfigLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    private Long logId;

    @ApiModelProperty(value = "参数ID")
    private Long configId;

    @ApiModelProperty(value = "参数键名")
    private String configKey;

    @ApiModelProperty(value = "变更前值")
    private String oldValue;

    @ApiModelProperty(value = "变更后值")
    private String newValue;

    @ApiModelProperty(value = "操作类型 (INSERT/UPDATE/DELETE)")
    private String operType;

    @ApiModelProperty(value = "操作人")
    private String operBy;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operTime;
}

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
 * 登录日志实体
 */
@Data
@TableName("sys_login_log")
@ApiModel(description = "登录日志")
public class SysLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    private Long logId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "登录IP")
    private String ipAddr;

    @ApiModelProperty(value = "登录状态 (0成功 1失败)")
    private Integer status;

    @ApiModelProperty(value = "提示消息")
    private String msg;

    @ApiModelProperty(value = "浏览器")
    private String browser;

    @ApiModelProperty(value = "操作系统")
    private String os;

    @ApiModelProperty(value = "登录时间")
    private LocalDateTime loginTime;
}

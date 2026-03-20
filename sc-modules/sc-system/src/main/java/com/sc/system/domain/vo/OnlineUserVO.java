package com.sc.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 在线用户信息
 */
@Data
@ApiModel(description = "在线用户信息")
public class OnlineUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会话Token")
    private String tokenId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "登录IP")
    private String ipAddr;

    @ApiModelProperty(value = "浏览器")
    private String browser;

    @ApiModelProperty(value = "操作系统")
    private String os;

    @ApiModelProperty(value = "登录时间")
    private String loginTime;
}

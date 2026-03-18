package com.sc.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 第三方账号绑定视图对象（脱敏返回，不含 Token）
 */
@Data
@ApiModel(description = "第三方账号绑定视图对象")
public class SysUserSocialVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "绑定记录ID")
    private Long id;

    @ApiModelProperty(value = "第三方平台类型", example = "wechat_open")
    private String socialType;

    @ApiModelProperty(value = "第三方平台昵称", example = "微信用户")
    private String socialNickname;

    @ApiModelProperty(value = "第三方平台头像")
    private String socialAvatar;

    @ApiModelProperty(value = "绑定时间")
    private LocalDateTime createTime;
}

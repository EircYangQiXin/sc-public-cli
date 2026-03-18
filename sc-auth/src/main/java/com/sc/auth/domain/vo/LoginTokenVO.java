package com.sc.auth.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回的 Token 信息
 */
@Data
@ApiModel(description = "登录Token信息")
public class LoginTokenVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "访问令牌", example = "eyJ0eXAiOiJKV1QiLCJhbGciOi...")
    private String accessToken;

    @ApiModelProperty(value = "令牌过期时间（秒）", example = "1800")
    private long expiresIn;
}

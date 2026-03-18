package com.sc.auth.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码响应
 */
@Data
@ApiModel(description = "图形验证码信息")
public class CaptchaVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "验证码唯一标识", example = "a1b2c3d4e5f6")
    private String uuid;

    @ApiModelProperty(value = "验证码图片（Base64编码）", example = "data:image/png;base64,iVBOR...")
    private String image;
}

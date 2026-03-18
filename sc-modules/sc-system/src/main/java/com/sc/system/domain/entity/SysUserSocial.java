package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sc.common.core.domain.BaseEntity;
import com.sc.common.mybatis.encrypt.EncryptField;
import com.sc.common.mybatis.encrypt.EncryptTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户第三方账号绑定实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_social", autoResultMap = true)
@ApiModel(description = "第三方账号绑定信息")
public class SysUserSocial extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "系统用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "第三方平台类型 (wechat_open/wechat_mp/wechat_mini/alipay/qq/weibo/github/dingtalk/wechat_work/apple/google)",
            required = true, example = "wechat_open")
    private String socialType;

    @ApiModelProperty(value = "第三方平台唯一标识（openId/userId/uid）", required = true, example = "oXxxx_xxxxxx")
    private String socialId;

    @ApiModelProperty(value = "第三方 unionId（微信跨应用统一标识）", example = "o6_xxx")
    private String unionId;

    @ApiModelProperty(value = "第三方平台昵称", example = "微信用户")
    private String socialNickname;

    @ApiModelProperty(value = "第三方平台头像")
    private String socialAvatar;

    @EncryptField
    @TableField(typeHandler = EncryptTypeHandler.class)
    @ApiModelProperty(value = "Access Token（加密存储）", hidden = true)
    private String accessToken;

    @EncryptField
    @TableField(typeHandler = EncryptTypeHandler.class)
    @ApiModelProperty(value = "Refresh Token（加密存储）", hidden = true)
    private String refreshToken;

    @ApiModelProperty(value = "Token 过期时间")
    private LocalDateTime tokenExpireTime;

    @ApiModelProperty(value = "第三方平台原始返回数据（JSON）", hidden = true)
    private String rawData;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;
}

package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sc.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@ApiModel(description = "系统用户")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "部门ID", example = "1")
    private Long deptId;

    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    private String username;

    @ApiModelProperty(value = "昵称", required = true, example = "管理员")
    private String nickName;

    @ApiModelProperty(value = "邮箱", example = "admin@sc.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "15888888888")
    private String phone;

    @ApiModelProperty(value = "性别 (0男 1女 2未知)", example = "0")
    private String sex;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "密码最后修改时间")
    private LocalDateTime passwordUpdateTime;

    @ApiModelProperty(value = "MFA密钥(Base32)")
    private String mfaSecret;

    @ApiModelProperty(value = "是否启用MFA (0否 1是)", example = "0")
    private Integer mfaEnabled;
}

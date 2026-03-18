package com.sc.common.core.domain.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息（存储在 Sa-Token Session 中）
 */
@Data
@ApiModel(description = "登录用户信息")
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "部门ID")
    private Long deptId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "用户类型 (sys_user/app_user)")
    private String userType;

    @ApiModelProperty(value = "租户ID（多租户预留）")
    private Long tenantId;

    @ApiModelProperty(value = "角色Key集合")
    private Set<String> roles;

    @ApiModelProperty(value = "权限标识集合")
    private Set<String> permissions;

    @ApiModelProperty(value = "数据权限范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）")
    private Integer dataScope;

    @ApiModelProperty(value = "数据权限关联的部门ID集合")
    private Set<Long> dataScopeDeptIds;
}

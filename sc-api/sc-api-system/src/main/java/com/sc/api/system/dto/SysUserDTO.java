package com.sc.api.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 系统用户 DTO（远程调用传输对象）
 */
@Data
@ApiModel(description = "系统用户传输对象（远程调用）")
public class SysUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "部门ID")
    private Long deptId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码", hidden = true)
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "状态 (0正常 1停用)")
    private String status;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "角色Key集合")
    private Set<String> roles;

    @ApiModelProperty(value = "权限标识集合")
    private Set<String> permissions;

    @ApiModelProperty(value = "数据权限范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）")
    private Integer dataScope;

    @ApiModelProperty(value = "数据权限关联部门ID集合")
    private Set<Long> dataScopeDeptIds;
}

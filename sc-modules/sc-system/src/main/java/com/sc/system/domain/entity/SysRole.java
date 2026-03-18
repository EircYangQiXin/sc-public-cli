package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sc.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@ApiModel(description = "角色信息")
public class SysRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "角色ID", example = "1")
    private Long roleId;

    @ApiModelProperty(value = "角色名称", required = true, example = "管理员")
    private String roleName;

    @ApiModelProperty(value = "角色权限字符串", required = true, example = "admin")
    private String roleKey;

    @ApiModelProperty(value = "显示顺序", example = "1")
    private Integer roleSort;

    @ApiModelProperty(value = "数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）", example = "1")
    private Integer dataScope;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;
}

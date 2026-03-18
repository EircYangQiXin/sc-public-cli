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
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@ApiModel(description = "菜单权限")
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long menuId;

    @ApiModelProperty(value = "菜单名称", required = true, example = "系统管理")
    private String menuName;

    @ApiModelProperty(value = "父菜单ID", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "显示顺序", example = "1")
    private Integer orderNum;

    @ApiModelProperty(value = "路由地址", example = "system")
    private String path;

    @ApiModelProperty(value = "组件路径", example = "system/user/index")
    private String component;

    @ApiModelProperty(value = "菜单类型（M目录 C菜单 F按钮）", example = "C")
    private String menuType;

    @ApiModelProperty(value = "可见状态（0显示 1隐藏）", example = "0")
    private String visible;

    @ApiModelProperty(value = "状态（0正常 1停用）", example = "0")
    private String status;

    @ApiModelProperty(value = "权限标识", example = "system:user:list")
    private String perms;

    @ApiModelProperty(value = "菜单图标", example = "user")
    private String icon;
}

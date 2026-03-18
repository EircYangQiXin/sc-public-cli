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
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@ApiModel(description = "部门信息")
public class SysDept extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "部门ID", example = "1")
    private Long deptId;

    @ApiModelProperty(value = "父部门ID", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "祖级列表", example = "0,1")
    private String ancestors;

    @ApiModelProperty(value = "部门名称", required = true, example = "研发部")
    private String deptName;

    @ApiModelProperty(value = "显示顺序", example = "1")
    private Integer orderNum;

    @ApiModelProperty(value = "负责人", example = "张三")
    private String leader;

    @ApiModelProperty(value = "联系电话", example = "15888888888")
    private String phone;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;
}

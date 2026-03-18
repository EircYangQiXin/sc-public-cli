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
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
@ApiModel(description = "字典类型")
public class SysDictType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "字典ID", example = "1")
    private Long dictId;

    @ApiModelProperty(value = "字典名称", required = true, example = "用户性别")
    private String dictName;

    @ApiModelProperty(value = "字典类型", required = true, example = "sys_user_sex")
    private String dictType;

    @ApiModelProperty(value = "状态（0正常 1停用）", example = "0")
    private String status;
}

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
 * 字典数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
@ApiModel(description = "字典数据")
public class SysDictData extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "字典编码", example = "1")
    private Long dictCode;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer dictSort;

    @ApiModelProperty(value = "字典标签", required = true, example = "男")
    private String dictLabel;

    @ApiModelProperty(value = "字典键值", required = true, example = "0")
    private String dictValue;

    @ApiModelProperty(value = "字典类型", required = true, example = "sys_user_sex")
    private String dictType;

    @ApiModelProperty(value = "样式", example = "primary")
    private String cssClass;

    @ApiModelProperty(value = "表格样式", example = "default")
    private String listClass;

    @ApiModelProperty(value = "是否默认（Y是 N否）", example = "N")
    private String isDefault;

    @ApiModelProperty(value = "状态（0正常 1停用）", example = "0")
    private String status;
}

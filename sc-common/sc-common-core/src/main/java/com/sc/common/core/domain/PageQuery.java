package com.sc.common.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
@ApiModel(description = "分页查询基础参数")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码最小值为1")
    @ApiModelProperty(value = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小值为1")
    @Max(value = 1000, message = "每页条数最大值为1000")
    @ApiModelProperty(value = "每页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "排序字段", example = "createTime")
    private String orderByColumn;

    @ApiModelProperty(value = "排序方向 (asc/desc)", example = "desc")
    private String isAsc;
}

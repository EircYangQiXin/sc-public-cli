package com.sc.common.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果包装
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@ApiModel(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总记录数", example = "100")
    private long total;

    @ApiModelProperty(value = "当前页数据列表")
    private List<T> rows;

    @ApiModelProperty(value = "当前页码", example = "1")
    private int pageNum;

    @ApiModelProperty(value = "每页大小", example = "10")
    private int pageSize;

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult(long total, List<T> rows, int pageNum, int pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}

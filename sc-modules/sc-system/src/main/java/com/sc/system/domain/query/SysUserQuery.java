package com.sc.system.domain.query;

import com.sc.common.core.domain.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户查询参数")
public class SysUserQuery extends PageQuery {

    @ApiModelProperty(value = "用户名（模糊匹配）", example = "admin")
    private String username;

    @ApiModelProperty(value = "手机号（模糊匹配）", example = "158")
    private String phone;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;

    @ApiModelProperty(value = "部门ID", example = "1")
    private Long deptId;
}

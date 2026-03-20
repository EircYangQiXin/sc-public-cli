package com.sc.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色菜单分配 DTO
 */
@Data
@ApiModel(description = "角色菜单分配请求体")
public class RoleMenuAssignDTO {

    @NotNull(message = "菜单ID列表不能为空")
    @ApiModelProperty(value = "要分配的菜单ID列表", required = true, example = "[1, 100, 101, 1001]")
    private List<Long> menuIds;
}

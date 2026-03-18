package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysDept;
import com.sc.system.domain.vo.SysDeptVO;
import com.sc.system.service.ISysDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Api(tags = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    @ApiOperation("查询部门列表（树形）")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/tree")
    public R<List<SysDeptVO>> tree(SysDept dept) {
        return R.ok(deptService.selectDeptTree(dept));
    }

    @ApiOperation("查询部门列表（平铺）")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public R<List<SysDeptVO>> list(SysDept dept) {
        return R.ok(deptService.selectDeptList(dept));
    }

    @ApiOperation("根据部门ID获取详情")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{deptId}")
    public R<SysDeptVO> getInfo(@ApiParam(value = "部门ID", required = true) @PathVariable Long deptId) {
        return R.ok(deptService.selectDeptById(deptId));
    }

    @ApiOperation("新增部门")
    @SaCheckPermission("system:dept:add")
    @OperationLog(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDept dept) {
        deptService.insertDept(dept);
        return R.ok();
    }

    @ApiOperation("修改部门")
    @SaCheckPermission("system:dept:edit")
    @OperationLog(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok();
    }

    @ApiOperation("删除部门")
    @SaCheckPermission("system:dept:remove")
    @OperationLog(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@ApiParam(value = "部门ID", required = true) @PathVariable Long deptId) {
        deptService.deleteDeptById(deptId);
        return R.ok();
    }
}

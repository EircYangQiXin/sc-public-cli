package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysMenu;
import com.sc.system.domain.vo.SysMenuVO;
import com.sc.system.service.ISysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    @ApiOperation("查询菜单列表（树形）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenuVO>> tree(@ApiParam(value = "菜单名称") @RequestParam(required = false) String menuName,
                                   @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {
        return R.ok(menuService.selectMenuTree(menuName, status));
    }

    @ApiOperation("查询菜单列表（平铺）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public R<List<SysMenuVO>> list(@ApiParam(value = "菜单名称") @RequestParam(required = false) String menuName,
                                    @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {
        return R.ok(menuService.selectMenuList(menuName, status));
    }

    @ApiOperation("根据菜单ID获取详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@ApiParam(value = "菜单ID", required = true) @PathVariable Long menuId) {
        return R.ok(menuService.getById(menuId));
    }

    @ApiOperation("新增菜单")
    @SaCheckPermission("system:menu:add")
    @OperationLog(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysMenu menu) {
        menuService.save(menu);
        return R.ok();
    }

    @ApiOperation("修改菜单")
    @SaCheckPermission("system:menu:edit")
    @OperationLog(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return R.ok();
    }

    @ApiOperation("删除菜单")
    @SaCheckPermission("system:menu:remove")
    @OperationLog(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@ApiParam(value = "菜单ID", required = true) @PathVariable Long menuId) {
        if (menuService.hasChildMenu(menuId)) {
            return R.fail("存在子菜单，不允许删除");
        }
        menuService.removeById(menuId);
        return R.ok();
    }
}

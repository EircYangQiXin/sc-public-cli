package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysMenu;
import com.sc.system.domain.vo.SysMenuVO;
import com.sc.system.mapper.SysMenuMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理控制器
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper menuMapper;

    @ApiOperation("查询菜单列表（树形）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenuVO>> tree(@ApiParam(value = "菜单名称") @RequestParam(required = false) String menuName,
                                   @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {
        List<SysMenu> list = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                        .eq(StringUtils.hasText(status), SysMenu::getStatus, status)
                        .orderByAsc(SysMenu::getOrderNum));

        List<SysMenuVO> voList = BeanUtil.copyToList(list, SysMenuVO.class);
        return R.ok(buildMenuTree(voList));
    }

    @ApiOperation("查询菜单列表（平铺）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public R<List<SysMenuVO>> list(@ApiParam(value = "菜单名称") @RequestParam(required = false) String menuName,
                                    @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {
        List<SysMenu> list = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                        .eq(StringUtils.hasText(status), SysMenu::getStatus, status)
                        .orderByAsc(SysMenu::getOrderNum));
        return R.ok(BeanUtil.copyToList(list, SysMenuVO.class));
    }

    @ApiOperation("根据菜单ID获取详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{menuId}")
    public R<SysMenuVO> getInfo(@ApiParam(value = "菜单ID", required = true) @PathVariable Long menuId) {
        return R.ok(BeanUtil.copyProperties(menuMapper.selectById(menuId), SysMenuVO.class));
    }

    @ApiOperation("新增菜单")
    @SaCheckPermission("system:menu:add")
    @OperationLog(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return R.ok();
    }

    @ApiOperation("修改菜单")
    @SaCheckPermission("system:menu:edit")
    @OperationLog(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysMenu menu) {
        menuMapper.updateById(menu);
        return R.ok();
    }

    @ApiOperation("删除菜单")
    @SaCheckPermission("system:menu:remove")
    @OperationLog(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@ApiParam(value = "菜单ID", required = true) @PathVariable Long menuId) {
        // 检查是否有子菜单
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        if (count > 0) {
            return R.fail("存在子菜单，不允许删除");
        }
        menuMapper.deleteById(menuId);
        return R.ok();
    }

    private List<SysMenuVO> buildMenuTree(List<SysMenuVO> list) {
        Map<Long, List<SysMenuVO>> childrenMap = list.stream()
                .collect(Collectors.groupingBy(SysMenuVO::getParentId));
        list.forEach(menu -> menu.setChildren(childrenMap.get(menu.getMenuId())));
        return list.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0L)
                .collect(Collectors.toList());
    }
}

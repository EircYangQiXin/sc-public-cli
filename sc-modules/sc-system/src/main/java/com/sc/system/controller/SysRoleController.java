package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysRole;
import com.sc.system.domain.entity.SysRoleDept;
import com.sc.system.domain.entity.SysRoleMenu;
import com.sc.system.mapper.SysRoleDeptMapper;
import com.sc.system.mapper.SysRoleMapper;
import com.sc.system.mapper.SysRoleMenuMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    // ==================== 角色 CRUD ====================

    @ApiOperation("分页查询角色列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "角色名称") @RequestParam(required = false) String roleName,
            @ApiParam(value = "角色权限字符串") @RequestParam(required = false) String roleKey,
            @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {

        Page<SysRole> page = roleMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>()
                        .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                        .like(StringUtils.hasText(roleKey), SysRole::getRoleKey, roleKey)
                        .eq(StringUtils.hasText(status), SysRole::getStatus, status)
                        .orderByAsc(SysRole::getRoleSort));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("根据角色ID获取详情")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId) {
        return R.ok(roleMapper.selectById(roleId));
    }

    @ApiOperation("新增角色")
    @SaCheckPermission("system:role:add")
    @OperationLog(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysRole role) {
        roleMapper.insert(role);
        return R.ok();
    }

    @ApiOperation("修改角色")
    @SaCheckPermission("system:role:edit")
    @OperationLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysRole role) {
        roleMapper.updateById(role);
        return R.ok();
    }

    @ApiOperation("删除角色")
    @SaCheckPermission("system:role:remove")
    @OperationLog(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> remove(@ApiParam(value = "角色ID数组", required = true) @PathVariable Long[] roleIds) {
        List<Long> ids = Arrays.asList(roleIds);
        // 同步删除关联数据
        for (Long roleId : ids) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
            roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        }
        roleMapper.deleteBatchIds(ids);
        return R.ok();
    }

    // ==================== 角色绑定菜单 ====================

    @ApiOperation("查询角色已分配的菜单ID列表")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}/menus")
    public R<List<Long>> getRoleMenuIds(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        return R.ok(menuIds);
    }

    @ApiOperation("为角色分配菜单权限")
    @SaCheckPermission("system:role:edit")
    @OperationLog(title = "角色菜单分配", businessType = BusinessType.GRANT)
    @PutMapping("/{roleId}/menus")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignMenus(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        // 先删除旧的关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        // 批量插入新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                roleMenuMapper.insert(new SysRoleMenu(roleId, menuId));
            }
        }
        return R.ok("菜单权限分配成功", null);
    }

    // ==================== 角色绑定部门（数据权限） ====================

    @ApiOperation("查询角色已分配的数据权限部门ID列表")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}/depts")
    public R<List<Long>> getRoleDeptIds(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId) {
        List<Long> deptIds = roleDeptMapper.selectDeptIdsByRoleId(roleId);
        return R.ok(deptIds);
    }

    @ApiOperation("为角色分配数据权限（设置数据范围和关联部门）")
    @SaCheckPermission("system:role:edit")
    @OperationLog(title = "角色数据权限分配", businessType = BusinessType.GRANT)
    @PutMapping("/{roleId}/dataScope")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignDataScope(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId, @RequestBody DataScopeBody body) {
        // 更新角色的数据范围
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setDataScope(body.getDataScope());
        roleMapper.updateById(role);

        // 如果是自定义数据范围 (dataScope=2)，更新关联部门
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        if (body.getDataScope() != null && body.getDataScope() == 2
                && body.getDeptIds() != null && !body.getDeptIds().isEmpty()) {
            for (Long deptId : body.getDeptIds()) {
                roleDeptMapper.insert(new SysRoleDept(roleId, deptId));
            }
        }
        return R.ok("数据权限分配成功", null);
    }

    /**
     * 数据权限请求体
     */
    @Data
    public static class DataScopeBody {
        /** 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人） */
        private Integer dataScope;
        /** 自定义数据权限关联的部门ID列表 */
        private List<Long> deptIds;
    }
}

package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysUser;
import com.sc.system.domain.entity.SysUserRole;
import com.sc.system.domain.vo.SysUserVO;
import com.sc.system.domain.query.SysUserQuery;
import com.sc.system.mapper.SysUserRoleMapper;
import com.sc.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;
    private final SysUserRoleMapper userRoleMapper;

    @ApiOperation("分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageResult<SysUserVO>> list(SysUserQuery query) {
        return R.ok(userService.selectUserPage(query));
    }

    @ApiOperation("根据用户名获取用户信息（内部调用）")
    @GetMapping("/info/{username}")
    public R<com.sc.api.system.dto.SysUserDTO> getUserInfo(@ApiParam(value = "用户名", required = true) @PathVariable String username) {
        return R.ok(userService.selectUserByUsername(username));
    }

    @ApiOperation("根据用户ID获取详情")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{userId}")
    public R<SysUserVO> getInfo(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        return R.ok(userService.selectUserById(userId));
    }

    @ApiOperation("新增用户")
    @SaCheckPermission("system:user:add")
    @OperationLog(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysUser user) {
        userService.insertUser(user);
        return R.ok();
    }

    @ApiOperation("修改用户")
    @SaCheckPermission("system:user:edit")
    @OperationLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok();
    }

    @ApiOperation("删除用户")
    @SaCheckPermission("system:user:remove")
    @OperationLog(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@ApiParam(value = "用户ID数组", required = true) @PathVariable Long[] userIds) {
        userService.deleteUserByIds(userIds);
        return R.ok();
    }

    // ==================== 用户角色分配 ====================

    @ApiOperation("查询用户已分配的角色ID列表")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{userId}/roles")
    public R<List<Long>> getUserRoleIds(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return R.ok(roleIds);
    }

    @ApiOperation("为用户分配角色")
    @SaCheckPermission("system:user:edit")
    @OperationLog(title = "用户角色分配", businessType = BusinessType.GRANT)
    @PutMapping("/{userId}/roles")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignRoles(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId, @RequestBody List<Long> roleIds) {
        // 先删除旧的关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 批量插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(userId, roleId));
            }
        }
        return R.ok("角色分配成功", null);
    }
}


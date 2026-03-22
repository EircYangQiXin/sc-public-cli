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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private static final String INTERNAL_HEADER = "X-SC-Internal";
    private static final String INTERNAL_SECRET = "sc-internal-feign";

    private final ISysUserService userService;
    private final SysUserRoleMapper userRoleMapper;
    private final HttpServletRequest request;

    @ApiOperation("分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageResult<SysUserVO>> list(SysUserQuery query) {
        return R.ok(userService.selectUserPage(query));
    }

    /**
     * 根据用户名获取用户信息（仅限内部 Feign 调用）
     * <p>
     * 安全策略:
     * <ul>
     *   <li>必须携带内部调用标识头 X-SC-Internal</li>
     *   <li>返回的 DTO 中 password / mfaSecret 仅在内部调用时包含</li>
     *   <li>网关应剥离外部请求中的 X-SC-Internal 头</li>
     * </ul>
     * </p>
     */
    @ApiOperation("根据用户名获取用户信息（内部调用）")
    @GetMapping("/info/{username}")
    public R<com.sc.api.system.dto.SysUserDTO> getUserInfo(
            @ApiParam(value = "用户名", required = true) @PathVariable String username) {
        // 校验内部调用标识
        String internalToken = request.getHeader(INTERNAL_HEADER);
        if (!INTERNAL_SECRET.equals(internalToken)) {
            return R.fail("非法访问");
        }

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
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(userId, roleId));
            }
        }
        return R.ok("角色分配成功", null);
    }

    /**
     * 更新用户 MFA 信息（仅限内部 Feign 调用）
     */
    @ApiOperation("更新用户 MFA 信息（内部调用）")
    @PutMapping("/mfa")
    public R<Void> updateUserMfa(@RequestBody com.sc.api.system.dto.SysUserDTO dto) {
        // 校验内部调用标识
        String internalToken = request.getHeader(INTERNAL_HEADER);
        if (!INTERNAL_SECRET.equals(internalToken)) {
            return R.fail("非法访问");
        }

        SysUser user = new SysUser();
        user.setUserId(dto.getUserId());
        user.setMfaSecret(dto.getMfaSecret());
        user.setMfaEnabled(dto.getMfaEnabled());
        userService.updateUser(user);
        return R.ok();
    }

    /**
     * 根据角色ID列表查询用户ID列表（仅限内部 Feign 调用）
     */
    @ApiOperation("根据角色ID列表查询用户ID列表（内部调用）")
    @PostMapping("/internal/user-ids-by-roles")
    public R<List<Long>> getUserIdsByRoleIds(@RequestBody List<Long> roleIds) {
        // 校验内部调用标识
        String internalToken = request.getHeader(INTERNAL_HEADER);
        if (!INTERNAL_SECRET.equals(internalToken)) {
            return R.fail("非法访问");
        }

        if (roleIds == null || roleIds.isEmpty()) {
            return R.ok(java.util.Collections.<Long>emptyList());
        }
        List<Long> userIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, roleIds)
                        .select(SysUserRole::getUserId))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        return R.ok(userIds);
    }
}

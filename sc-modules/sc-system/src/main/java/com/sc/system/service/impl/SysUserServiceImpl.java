package com.sc.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.exception.ServiceException;
import com.sc.common.core.utils.PasswordUtils;
import com.sc.system.domain.entity.SysUser;
import com.sc.system.domain.query.SysUserQuery;
import com.sc.system.domain.vo.SysUserVO;
import com.sc.system.mapper.SysMenuMapper;
import com.sc.system.mapper.SysRoleMapper;
import com.sc.system.mapper.SysUserMapper;
import com.sc.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements ISysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public PageResult<SysUserVO> selectUserPage(SysUserQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(StringUtils.hasText(query.getStatus()), SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        return new PageResult<>(
                result.getTotal(),
                BeanUtil.copyToList(result.getRecords(), SysUserVO.class),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    @Override
    public SysUserDTO selectUserByUsername(String username) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            return null;
        }
        SysUserDTO dto = new SysUserDTO();
        BeanUtil.copyProperties(user, dto);

        // 查询用户角色
        Set<String> roles = roleMapper.selectRoleKeysByUserId(user.getUserId());
        dto.setRoles(roles != null ? roles : new HashSet<String>());

        // 超级管理员拥有所有权限
        Set<String> permissions;
        if (roles != null && roles.contains("admin")) {
            permissions = new HashSet<String>();
            permissions.add("*:*:*");
        } else {
            permissions = menuMapper.selectPermsByUserId(user.getUserId());
        }
        dto.setPermissions(permissions != null ? permissions : new HashSet<String>());

        return dto;
    }

    @Override
    public SysUserVO selectUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return BeanUtil.copyProperties(user, SysUserVO.class);
    }

    @Override
    public void insertUser(SysUser user) {
        // 密码策略校验由调用方（如 AuthController 修改密码接口）负责
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        // 如果传入了新密码则加密并更新密码修改时间
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
            user.setPasswordUpdateTime(LocalDateTime.now());
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteUserByIds(Long[] userIds) {
        userMapper.deleteBatchIds(Arrays.asList(userIds));
    }
}

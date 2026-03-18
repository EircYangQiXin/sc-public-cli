package com.sc.system.service;

import com.sc.api.system.dto.SysUserDTO;
import com.sc.common.core.domain.PageResult;
import com.sc.system.domain.entity.SysUser;
import com.sc.system.domain.query.SysUserQuery;
import com.sc.system.domain.vo.SysUserVO;

/**
 * 用户业务接口
 */
public interface ISysUserService {

    /**
     * 分页查询用户列表
     */
    PageResult<SysUserVO> selectUserPage(SysUserQuery query);

    /**
     * 根据用户名查询用户（含角色权限，供认证服务调用）
     */
    SysUserDTO selectUserByUsername(String username);

    /**
     * 根据ID查询用户详情
     */
    SysUserVO selectUserById(Long userId);

    /**
     * 新增用户
     */
    void insertUser(SysUser user);

    /**
     * 修改用户
     */
    void updateUser(SysUser user);

    /**
     * 批量删除用户
     */
    void deleteUserByIds(Long[] userIds);
}

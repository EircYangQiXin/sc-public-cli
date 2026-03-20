package com.sc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.system.domain.entity.SysRole;

import java.util.List;

/**
 * 角色管理 Service 接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 为角色分配菜单权限
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 查询角色已分配的菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 为角色分配数据权限
     *
     * @param roleId    角色ID
     * @param dataScope 数据范围
     * @param deptIds   部门ID列表
     */
    void assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds);

    /**
     * 查询角色已分配的部门ID列表
     */
    List<Long> selectDeptIdsByRoleId(Long roleId);

    /**
     * 删除角色（含关联数据）
     */
    void deleteRoleByIds(List<Long> roleIds);
}

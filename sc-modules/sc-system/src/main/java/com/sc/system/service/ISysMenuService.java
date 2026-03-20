package com.sc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.system.domain.entity.SysMenu;
import com.sc.system.domain.vo.SysMenuVO;

import java.util.List;

/**
 * 菜单管理 Service 接口
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单树
     */
    List<SysMenuVO> selectMenuTree(String menuName, String status);

    /**
     * 查询菜单列表（平铺）
     */
    List<SysMenuVO> selectMenuList(String menuName, String status);

    /**
     * 校验菜单是否有子节点
     */
    boolean hasChildMenu(Long menuId);
}

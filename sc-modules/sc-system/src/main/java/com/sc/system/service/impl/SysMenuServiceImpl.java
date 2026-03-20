package com.sc.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.system.domain.entity.SysMenu;
import com.sc.system.domain.vo.SysMenuVO;
import com.sc.system.mapper.SysMenuMapper;
import com.sc.system.service.ISysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理 Service 实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenuVO> selectMenuTree(String menuName, String status) {
        List<SysMenuVO> voList = selectMenuList(menuName, status);
        return buildMenuTree(voList);
    }

    @Override
    public List<SysMenuVO> selectMenuList(String menuName, String status) {
        List<SysMenu> list = baseMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                        .eq(StringUtils.hasText(status), SysMenu::getStatus, status)
                        .orderByAsc(SysMenu::getOrderNum));
        return BeanUtil.copyToList(list, SysMenuVO.class);
    }

    @Override
    public boolean hasChildMenu(Long menuId) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        return count != null && count > 0;
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

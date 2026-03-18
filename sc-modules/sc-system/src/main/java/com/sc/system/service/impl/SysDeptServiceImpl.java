package com.sc.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.common.core.exception.ServiceException;
import com.sc.system.domain.entity.SysDept;
import com.sc.system.domain.vo.SysDeptVO;
import com.sc.system.mapper.SysDeptMapper;
import com.sc.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements ISysDeptService {

    private final SysDeptMapper deptMapper;

    @Override
    public List<SysDeptVO> selectDeptTree(SysDept dept) {
        List<SysDeptVO> list = selectDeptList(dept);
        return buildDeptTree(list);
    }

    @Override
    public List<SysDeptVO> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>()
                .like(StringUtils.hasText(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                .eq(StringUtils.hasText(dept.getStatus()), SysDept::getStatus, dept.getStatus())
                .orderByAsc(SysDept::getOrderNum);

        List<SysDept> depts = deptMapper.selectList(wrapper);
        return BeanUtil.copyToList(depts, SysDeptVO.class);
    }

    @Override
    public SysDeptVO selectDeptById(Long deptId) {
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new ServiceException("部门不存在");
        }
        return BeanUtil.copyProperties(dept, SysDeptVO.class);
    }

    @Override
    public void insertDept(SysDept dept) {
        // 设置祖级列表
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = deptMapper.selectById(dept.getParentId());
            if (parent == null) {
                throw new ServiceException("父部门不存在");
            }
            dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        } else {
            dept.setParentId(0L);
            dept.setAncestors("0");
        }
        deptMapper.insert(dept);
    }

    @Override
    public void updateDept(SysDept dept) {
        SysDept oldDept = deptMapper.selectById(dept.getDeptId());
        if (oldDept == null) {
            throw new ServiceException("部门不存在");
        }

        // 如果父部门变更，更新祖级列表
        if (dept.getParentId() != null && !dept.getParentId().equals(oldDept.getParentId())) {
            SysDept newParent = deptMapper.selectById(dept.getParentId());
            if (newParent != null) {
                String newAncestors = newParent.getAncestors() + "," + newParent.getDeptId();
                String oldAncestors = oldDept.getAncestors();
                dept.setAncestors(newAncestors);
                // 更新子部门的祖级列表
                updateChildAncestors(dept.getDeptId(), newAncestors, oldAncestors);
            }
        }
        deptMapper.updateById(dept);
    }

    @Override
    public void deleteDeptById(Long deptId) {
        // 检查是否有子部门
        Long count = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId));
        if (count > 0) {
            throw new ServiceException("存在下级部门，不允许删除");
        }
        deptMapper.deleteById(deptId);
    }

    /**
     * 构建部门树
     */
    private List<SysDeptVO> buildDeptTree(List<SysDeptVO> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        Map<Long, List<SysDeptVO>> childrenMap = list.stream()
                .collect(Collectors.groupingBy(SysDeptVO::getParentId));

        list.forEach(dept -> dept.setChildren(childrenMap.get(dept.getDeptId())));

        // 返回顶级节点（parentId = 0 的）
        return list.stream()
                .filter(dept -> dept.getParentId() == null || dept.getParentId() == 0L)
                .collect(Collectors.toList());
    }

    /**
     * 递归更新子部门的祖级列表
     */
    private void updateChildAncestors(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> children = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().apply("FIND_IN_SET({0}, ancestors)", deptId));
        for (SysDept child : children) {
            child.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors));
            deptMapper.updateById(child);
        }
    }
}

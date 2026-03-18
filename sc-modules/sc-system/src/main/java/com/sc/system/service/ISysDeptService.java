package com.sc.system.service;

import com.sc.system.domain.entity.SysDept;
import com.sc.system.domain.vo.SysDeptVO;

import java.util.List;

/**
 * 部门业务接口
 */
public interface ISysDeptService {

    /**
     * 查询部门列表（树形）
     */
    List<SysDeptVO> selectDeptTree(SysDept dept);

    /**
     * 查询部门列表（平铺）
     */
    List<SysDeptVO> selectDeptList(SysDept dept);

    /**
     * 根据ID查询部门
     */
    SysDeptVO selectDeptById(Long deptId);

    /**
     * 新增部门
     */
    void insertDept(SysDept dept);

    /**
     * 修改部门
     */
    void updateDept(SysDept dept);

    /**
     * 删除部门
     */
    void deleteDeptById(Long deptId);
}

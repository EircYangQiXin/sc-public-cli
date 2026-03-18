package com.sc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sc.system.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT DISTINCT r.role_key FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = '0' AND r.del_flag = 0")
    Set<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}

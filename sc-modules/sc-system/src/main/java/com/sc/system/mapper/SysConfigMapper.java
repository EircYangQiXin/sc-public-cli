package com.sc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sc.system.domain.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置参数 Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}

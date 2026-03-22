package com.sc.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sc.message.domain.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内信消息 Mapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}

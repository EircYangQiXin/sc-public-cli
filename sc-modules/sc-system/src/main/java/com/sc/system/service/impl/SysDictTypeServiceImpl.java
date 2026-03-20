package com.sc.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.system.domain.entity.SysDictType;
import com.sc.system.mapper.SysDictTypeMapper;
import com.sc.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典类型 Service 实现
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {
}

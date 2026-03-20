package com.sc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.system.domain.entity.SysDictData;

import java.util.List;

/**
 * 字典数据 Service 接口
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 根据字典类型查询数据列表
     */
    List<SysDictData> selectByDictType(String dictType);
}

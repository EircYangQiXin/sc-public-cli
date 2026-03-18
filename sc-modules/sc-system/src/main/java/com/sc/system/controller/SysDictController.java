package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysDictType;
import com.sc.system.domain.entity.SysDictData;
import com.sc.system.mapper.SysDictTypeMapper;
import com.sc.system.mapper.SysDictDataMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 字典管理控制器
 */
@Api(tags = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    // ==================== 字典类型 ====================

    @ApiOperation("分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/list")
    public R<PageResult<SysDictType>> typeList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "字典名称") @RequestParam(required = false) String dictName,
            @ApiParam(value = "字典类型") @RequestParam(required = false) String dictType,
            @ApiParam(value = "状态 (0正常 1停用)") @RequestParam(required = false) String status) {

        Page<SysDictType> page = dictTypeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysDictType>()
                        .like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName)
                        .like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType)
                        .eq(StringUtils.hasText(status), SysDictType::getStatus, status));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("根据ID查询字典类型")
    @GetMapping("/type/{dictId}")
    public R<SysDictType> getType(@ApiParam(value = "字典ID", required = true) @PathVariable Long dictId) {
        return R.ok(dictTypeMapper.selectById(dictId));
    }

    @ApiOperation("新增字典类型")
    @SaCheckPermission("system:dict:add")
    @OperationLog(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping("/type")
    public R<Void> addType(@Validated @RequestBody SysDictType dictType) {
        dictTypeMapper.insert(dictType);
        return R.ok();
    }

    @ApiOperation("修改字典类型")
    @SaCheckPermission("system:dict:edit")
    @OperationLog(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> editType(@Validated @RequestBody SysDictType dictType) {
        dictTypeMapper.updateById(dictType);
        return R.ok();
    }

    @ApiOperation("删除字典类型")
    @SaCheckPermission("system:dict:remove")
    @OperationLog(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{dictIds}")
    public R<Void> removeType(@ApiParam(value = "字典ID数组", required = true) @PathVariable Long[] dictIds) {
        dictTypeMapper.deleteBatchIds(Arrays.asList(dictIds));
        return R.ok();
    }

    // ==================== 字典数据 ====================

    @ApiOperation("根据字典类型查询数据列表")
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dataByType(@ApiParam(value = "字典类型", required = true) @PathVariable String dictType) {
        List<SysDictData> list = dictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .orderByAsc(SysDictData::getDictSort));
        return R.ok(list);
    }

    @ApiOperation("分页查询字典数据")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/data/list")
    public R<PageResult<SysDictData>> dataList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "字典类型") @RequestParam(required = false) String dictType,
            @ApiParam(value = "字典标签") @RequestParam(required = false) String dictLabel) {

        Page<SysDictData> page = dictDataMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysDictData>()
                        .eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType)
                        .like(StringUtils.hasText(dictLabel), SysDictData::getDictLabel, dictLabel)
                        .orderByAsc(SysDictData::getDictSort));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("新增字典数据")
    @SaCheckPermission("system:dict:add")
    @OperationLog(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public R<Void> addData(@Validated @RequestBody SysDictData dictData) {
        dictDataMapper.insert(dictData);
        return R.ok();
    }

    @ApiOperation("修改字典数据")
    @SaCheckPermission("system:dict:edit")
    @OperationLog(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> editData(@Validated @RequestBody SysDictData dictData) {
        dictDataMapper.updateById(dictData);
        return R.ok();
    }

    @ApiOperation("删除字典数据")
    @SaCheckPermission("system:dict:remove")
    @OperationLog(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{dictCodes}")
    public R<Void> removeData(@ApiParam(value = "字典编码数组", required = true) @PathVariable Long[] dictCodes) {
        dictDataMapper.deleteBatchIds(Arrays.asList(dictCodes));
        return R.ok();
    }
}

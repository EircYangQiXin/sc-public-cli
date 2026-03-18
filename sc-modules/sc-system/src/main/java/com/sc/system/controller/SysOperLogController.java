package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysOperLog;
import com.sc.system.mapper.SysOperLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("/system/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    @ApiOperation("分页查询操作日志")
    @SaCheckPermission("system:operlog:list")
    @GetMapping("/list")
    public R<PageResult<SysOperLog>> list(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "模块标题") @RequestParam(required = false) String title,
            @ApiParam(value = "操作人") @RequestParam(required = false) String operName,
            @ApiParam(value = "业务类型") @RequestParam(required = false) Integer businessType,
            @ApiParam(value = "操作状态 (0正常 1异常)") @RequestParam(required = false) Integer status) {

        Page<SysOperLog> page = operLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperLog>()
                        .like(StringUtils.hasText(title), SysOperLog::getTitle, title)
                        .like(StringUtils.hasText(operName), SysOperLog::getOperName, operName)
                        .eq(businessType != null, SysOperLog::getBusinessType, businessType)
                        .eq(status != null, SysOperLog::getStatus, status)
                        .orderByDesc(SysOperLog::getOperTime));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("根据ID查询操作日志详情")
    @SaCheckPermission("system:operlog:query")
    @GetMapping("/{operId}")
    public R<SysOperLog> getInfo(@ApiParam(value = "操作日志ID", required = true) @PathVariable Long operId) {
        return R.ok(operLogMapper.selectById(operId));
    }

    @ApiOperation("清空操作日志")
    @SaCheckPermission("system:operlog:remove")
    @OperationLog(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogMapper.delete(null);
        return R.ok();
    }
}

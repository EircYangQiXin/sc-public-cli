package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysLoginLog;
import com.sc.system.mapper.SysLoginLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@Api(tags = "登录日志")
@RestController
@RequestMapping("/system/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogMapper loginLogMapper;

    @ApiOperation("分页查询登录日志")
    @SaCheckPermission("system:loginlog:list")
    @GetMapping("/list")
    public R<PageResult<SysLoginLog>> list(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "用户名") @RequestParam(required = false) String username,
            @ApiParam(value = "登录IP") @RequestParam(required = false) String ipAddr,
            @ApiParam(value = "登录状态 (0成功 1失败)") @RequestParam(required = false) Integer status) {

        Page<SysLoginLog> page = loginLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysLoginLog>()
                        .like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                        .like(StringUtils.hasText(ipAddr), SysLoginLog::getIpAddr, ipAddr)
                        .eq(status != null, SysLoginLog::getStatus, status)
                        .orderByDesc(SysLoginLog::getLoginTime));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("清空登录日志")
    @SaCheckPermission("system:loginlog:remove")
    @OperationLog(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginLogMapper.delete(null);
        return R.ok();
    }
}

package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.system.domain.vo.XxlJobLogVO;
import com.sc.system.service.XxlJobAdminClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务执行日志
 */
@Api(tags = "任务执行日志")
@RestController
@RequestMapping("/monitor/jobLog")
@RequiredArgsConstructor
public class SysJobLogController {

    private final XxlJobAdminClient xxlJobAdminClient;

    @ApiOperation("分页查询执行日志")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/list")
    public R<PageResult<XxlJobLogVO>> list(
            @ApiParam(value = "执行器ID", required = true) @RequestParam(defaultValue = "1") Integer jobGroup,
            @ApiParam(value = "任务ID，0=全部") @RequestParam(defaultValue = "0") Integer jobId,
            @ApiParam(value = "日志状态：0=全部，1=成功，2=失败，3=进行中") @RequestParam(defaultValue = "0") Integer logStatus,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        int start = (pageNum - 1) * pageSize;
        PageResult<XxlJobLogVO> result = xxlJobAdminClient.logPageList(
                jobGroup, jobId, logStatus, start, pageSize);
        return R.ok(result);
    }

    @ApiOperation("查看执行日志详情")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/detail")
    public R<String> logDetail(
            @ApiParam(value = "日志ID", required = true) @RequestParam Long logId,
            @ApiParam(value = "起始行号") @RequestParam(defaultValue = "1") Integer fromLineNum) {
        return xxlJobAdminClient.logDetailCat(logId, fromLineNum);
    }
}

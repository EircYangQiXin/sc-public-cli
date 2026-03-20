package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.dto.XxlJobSaveDTO;
import com.sc.system.domain.vo.XxlJobInfoVO;
import com.sc.system.service.XxlJobAdminClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务管理
 */
@Api(tags = "定时任务管理")
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final XxlJobAdminClient xxlJobAdminClient;

    @ApiOperation("分页查询任务列表")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/list")
    public R<PageResult<XxlJobInfoVO>> list(
            @ApiParam(value = "执行器ID", required = true) @RequestParam(defaultValue = "1") Integer jobGroup,
            @ApiParam(value = "调度状态：-1=全部，0=停止，1=运行") @RequestParam(defaultValue = "-1") Integer triggerStatus,
            @ApiParam(value = "任务描述") @RequestParam(required = false) String jobDesc,
            @ApiParam(value = "JobHandler名称") @RequestParam(required = false) String executorHandler,
            @ApiParam(value = "负责人") @RequestParam(required = false) String author,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        int start = (pageNum - 1) * pageSize;
        PageResult<XxlJobInfoVO> result = xxlJobAdminClient.pageList(
                jobGroup, triggerStatus, jobDesc, executorHandler, author, start, pageSize);
        return R.ok(result);
    }

    @ApiOperation("新增任务")
    @SaCheckPermission("monitor:job:add")
    @OperationLog(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Integer> add(@RequestBody XxlJobSaveDTO dto) {
        return xxlJobAdminClient.addJob(dto);
    }

    @ApiOperation("修改任务")
    @SaCheckPermission("monitor:job:edit")
    @OperationLog(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody XxlJobSaveDTO dto) {
        return xxlJobAdminClient.updateJob(dto);
    }

    @ApiOperation("删除任务")
    @SaCheckPermission("monitor:job:remove")
    @OperationLog(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobId}")
    public R<Void> remove(@ApiParam(value = "任务ID", required = true) @PathVariable Integer jobId) {
        return xxlJobAdminClient.removeJob(jobId);
    }

    @ApiOperation("启动任务")
    @SaCheckPermission("monitor:job:edit")
    @OperationLog(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{jobId}")
    public R<Void> start(@ApiParam(value = "任务ID", required = true) @PathVariable Integer jobId) {
        return xxlJobAdminClient.startJob(jobId);
    }

    @ApiOperation("停止任务")
    @SaCheckPermission("monitor:job:edit")
    @OperationLog(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/stop/{jobId}")
    public R<Void> stop(@ApiParam(value = "任务ID", required = true) @PathVariable Integer jobId) {
        return xxlJobAdminClient.stopJob(jobId);
    }

    @ApiOperation("手动触发一次任务")
    @SaCheckPermission("monitor:job:edit")
    @OperationLog(title = "定时任务", businessType = BusinessType.OTHER)
    @PostMapping("/trigger/{jobId}")
    public R<Void> trigger(
            @ApiParam(value = "任务ID", required = true) @PathVariable Integer jobId,
            @ApiParam(value = "执行参数") @RequestParam(required = false) String executorParam) {
        return xxlJobAdminClient.triggerJob(jobId, executorParam);
    }
}

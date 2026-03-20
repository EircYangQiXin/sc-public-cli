package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.PageResult;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysConfig;
import com.sc.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置参数控制器
 */
@Api(tags = "系统配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigService configService;

    @ApiOperation("分页查询配置列表")
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public R<PageResult<SysConfig>> list(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "参数名称") @RequestParam(required = false) String configName,
            @ApiParam(value = "参数键名") @RequestParam(required = false) String configKey) {

        Page<SysConfig> page = configService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysConfig>()
                        .like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                        .like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @ApiOperation("根据ID查询配置")
    @GetMapping("/{configId}")
    public R<SysConfig> getInfo(@ApiParam(value = "参数ID", required = true) @PathVariable Long configId) {
        return R.ok(configService.getById(configId));
    }

    @ApiOperation("根据参数键名查询值")
    @GetMapping("/key/{configKey}")
    public R<String> getValueByKey(@ApiParam(value = "参数键名", required = true) @PathVariable String configKey) {
        return R.ok(configService.getConfigValueByKey(configKey));
    }

    @ApiOperation("新增配置")
    @SaCheckPermission("system:config:add")
    @OperationLog(title = "系统配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysConfig config) {
        configService.insertConfig(config);
        return R.ok();
    }

    @ApiOperation("修改配置")
    @SaCheckPermission("system:config:edit")
    @OperationLog(title = "系统配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysConfig config) {
        configService.updateConfig(config);
        return R.ok();
    }

    @ApiOperation("删除配置")
    @SaCheckPermission("system:config:remove")
    @OperationLog(title = "系统配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@ApiParam(value = "参数ID数组", required = true) @PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return R.ok();
    }

    @ApiOperation("刷新配置缓存")
    @SaCheckPermission("system:config:edit")
    @DeleteMapping("/cache")
    public R<Void> refreshCache() {
        configService.refreshCache();
        return R.ok();
    }
}

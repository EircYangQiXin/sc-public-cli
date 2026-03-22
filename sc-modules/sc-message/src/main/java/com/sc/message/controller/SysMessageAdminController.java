package com.sc.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.message.domain.dto.MessageSendDTO;
import com.sc.message.domain.entity.SysMessage;
import com.sc.message.service.ISysMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 站内信管理侧控制器
 */
@Api(tags = "站内信-管理侧")
@RestController
@RequestMapping("/message/admin")
@RequiredArgsConstructor
public class SysMessageAdminController {

    private final ISysMessageService messageService;

    @ApiOperation("发送站内信")
    @SaCheckPermission("message:admin:send")
    @OperationLog(title = "站内信", businessType = BusinessType.INSERT)
    @PostMapping("/send")
    public R<Void> send(@Validated @RequestBody MessageSendDTO dto) {
        messageService.sendMessage(dto);
        return R.ok();
    }

    @ApiOperation("已发消息列表")
    @SaCheckPermission("message:admin:list")
    @GetMapping("/list")
    public R<PageResult<SysMessage>> list(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "消息标题") @RequestParam(required = false) String title,
            @ApiParam(value = "消息类型") @RequestParam(required = false) Integer msgType) {

        Page<SysMessage> page = messageService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysMessage>()
                        .like(StringUtils.hasText(title), SysMessage::getTitle, title)
                        .eq(msgType != null, SysMessage::getMsgType, msgType)
                        .orderByDesc(SysMessage::getCreateTime));

        return R.ok(new PageResult<>(page.getTotal(), page.getRecords(), (int) page.getCurrent(), (int) page.getSize()));
    }
}

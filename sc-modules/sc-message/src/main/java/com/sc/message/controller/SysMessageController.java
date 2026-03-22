package com.sc.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.message.domain.query.MessageQuery;
import com.sc.message.domain.vo.MessageUnreadVO;
import com.sc.message.domain.vo.MessageVO;
import com.sc.message.service.ISysMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内信用户侧控制器
 */
@Api(tags = "站内信-用户侧")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class SysMessageController {

    private final ISysMessageService messageService;

    @ApiOperation("我的消息列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<PageResult<MessageVO>> list(MessageQuery query) {
        Long userId = SecurityContextHolder.getUserId();
        return R.ok(messageService.listMyMessages(userId, query));
    }

    @ApiOperation("未读消息数统计")
    @SaCheckLogin
    @GetMapping("/unread")
    public R<MessageUnreadVO> unreadCount() {
        Long userId = SecurityContextHolder.getUserId();
        return R.ok(messageService.countUnread(userId));
    }

    @ApiOperation("消息详情（自动标记已读）")
    @SaCheckLogin
    @GetMapping("/{messageId}")
    public R<MessageVO> detail(@ApiParam(value = "消息ID", required = true) @PathVariable Long messageId) {
        Long userId = SecurityContextHolder.getUserId();
        return R.ok(messageService.getMessageDetail(userId, messageId));
    }

    @ApiOperation("批量标记已读")
    @SaCheckLogin
    @PutMapping("/read")
    public R<Void> markRead(@ApiParam(value = "消息ID列表", required = true) @RequestBody List<Long> messageIds) {
        Long userId = SecurityContextHolder.getUserId();
        messageService.markAsRead(userId, messageIds);
        return R.ok();
    }

    @ApiOperation("全部标记已读")
    @SaCheckLogin
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        Long userId = SecurityContextHolder.getUserId();
        messageService.markAllAsRead(userId);
        return R.ok();
    }
}

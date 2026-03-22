package com.sc.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.api.system.RemoteUserService;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.common.core.exception.ServiceException;
import com.sc.message.domain.dto.MessageSendDTO;
import com.sc.message.domain.entity.SysMessage;
import com.sc.message.domain.entity.SysMessageReceiver;
import com.sc.message.domain.query.MessageQuery;
import com.sc.message.domain.vo.MessageUnreadVO;
import com.sc.message.domain.vo.MessageVO;
import com.sc.message.mapper.SysMessageMapper;
import com.sc.message.mapper.SysMessageReceiverMapper;
import com.sc.message.service.ISysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 站内信服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

    private final SysMessageReceiverMapper receiverMapper;
    private final RemoteUserService remoteUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(MessageSendDTO dto) {
        // 构建消息主体
        SysMessage message = new SysMessage();
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 0);
        message.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        message.setSendScope(dto.getSendScope());
        message.setSenderId(SecurityContextHolder.getUserId());
        message.setSenderName(SecurityContextHolder.getUsername());
        this.save(message);

        // 全员公告不需要插入接收人记录
        if ("ALL".equals(dto.getSendScope())) {
            log.info("全员公告已发布: messageId={}, title={}", message.getMessageId(), message.getTitle());
            return;
        }

        // 展开接收人列表
        List<Long> receiverIds = resolveReceiverIds(dto);
        if (receiverIds.isEmpty()) {
            throw new ServiceException("未找到有效的接收用户");
        }

        // 批量写入接收人记录
        batchInsertReceivers(message.getMessageId(), receiverIds);
        log.info("站内信已发送: messageId={}, scope={}, receiverCount={}",
                message.getMessageId(), dto.getSendScope(), receiverIds.size());
    }

    @Override
    public PageResult<MessageVO> listMyMessages(Long userId, MessageQuery query) {
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        List<MessageVO> rows = receiverMapper.selectUserMessages(userId, query, offset);
        long total = receiverMapper.countUserMessages(userId, query);
        return new PageResult<MessageVO>(total, rows, query.getPageNum(), query.getPageSize());
    }

    @Override
    public MessageUnreadVO countUnread(Long userId) {
        // 普通接收的未读 + 全员公告的未读
        int noticeUnread = receiverMapper.countUnreadByType(userId, 0) + receiverMapper.countBroadcastUnread(userId, 0);
        int bulletinUnread = receiverMapper.countUnreadByType(userId, 1) + receiverMapper.countBroadcastUnread(userId, 1);
        int letterUnread = receiverMapper.countUnreadByType(userId, 2) + receiverMapper.countBroadcastUnread(userId, 2);

        return MessageUnreadVO.builder()
                .totalUnread(noticeUnread + bulletinUnread + letterUnread)
                .noticeUnread(noticeUnread)
                .bulletinUnread(bulletinUnread)
                .letterUnread(letterUnread)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO getMessageDetail(Long userId, Long messageId) {
        MessageVO vo = receiverMapper.selectMessageDetail(userId, messageId);
        if (vo == null) {
            // selectMessageDetail 已包含 ACL 过滤（接收人 OR 全员公告），查不到即无权限
            throw new ServiceException("消息不存在或无权查看");
        }

        // 自动标记已读
        if (vo.getIsRead() == null || vo.getIsRead() == 0) {
            markSingleAsRead(userId, messageId);
            vo.setIsRead(1);
            vo.setReadTime(LocalDateTime.now());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        for (Long messageId : messageIds) {
            markSingleAsRead(userId, messageId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        receiverMapper.update(null,
                new LambdaUpdateWrapper<SysMessageReceiver>()
                        .set(SysMessageReceiver::getIsRead, 1)
                        .set(SysMessageReceiver::getReadTime, LocalDateTime.now())
                        .eq(SysMessageReceiver::getReceiverId, userId)
                        .eq(SysMessageReceiver::getIsRead, 0));

        // 为全员公告也创建已读记录
        markBroadcastAsRead(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void internalSend(String title, String content, Integer priority, List<Long> receiverIds) {
        SysMessage message = new SysMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setMsgType(2); // 私信
        message.setPriority(priority != null ? priority : 0);
        message.setSendScope("USER");
        message.setSenderId(0L);
        message.setSenderName("系统");
        this.save(message);

        if (receiverIds != null && !receiverIds.isEmpty()) {
            batchInsertReceivers(message.getMessageId(), receiverIds);
        }
        log.info("内部站内信已入库: messageId={}, receiverCount={}",
                message.getMessageId(), receiverIds != null ? receiverIds.size() : 0);
    }

    // ==================== 私有方法 ====================

    /**
     * 展开接收人ID列表
     */
    private List<Long> resolveReceiverIds(MessageSendDTO dto) {
        if ("USER".equals(dto.getSendScope())) {
            return dto.getReceiverIds() != null ? dto.getReceiverIds() : Collections.<Long>emptyList();
        }
        if ("ROLE".equals(dto.getSendScope())) {
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                throw new ServiceException("按角色发送时，角色ID列表不能为空");
            }
            return selectUserIdsByRoleIds(dto.getRoleIds());
        }
        return Collections.emptyList();
    }

    /**
     * 通过 Feign 调 sc-system 查询角色关联的用户ID列表
     */
    private List<Long> selectUserIdsByRoleIds(List<Long> roleIds) {
        R<List<Long>> result = remoteUserService.getUserIdsByRoleIds(roleIds);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            String msg = result != null ? result.getMsg() : "远程调用返回 null";
            log.error("通过 Feign 查询角色用户失败: {}", msg);
            throw new ServiceException("查询角色下的用户失败: " + msg);
        }
        log.info("按角色查询用户: roleIds={}, userCount={}", roleIds, result.getData().size());
        return result.getData();
    }

    /**
     * 批量插入接收人记录（自动去重）
     */
    private void batchInsertReceivers(Long messageId, List<Long> receiverIds) {
        // 去重，防止唯一索引冲突
        for (Long receiverId : new LinkedHashSet<Long>(receiverIds)) {
            SysMessageReceiver receiver = SysMessageReceiver.builder()
                    .messageId(messageId)
                    .receiverId(receiverId)
                    .isRead(0)
                    .build();
            receiverMapper.insert(receiver);
        }
    }

    /**
     * 标记单条消息已读
     */
    private void markSingleAsRead(Long userId, Long messageId) {
        // 先查是否有接收记录（使用 selectList + 取首条，兼容唯一索引前的历史脏数据）
        List<SysMessageReceiver> receivers = receiverMapper.selectList(
                new LambdaQueryWrapper<SysMessageReceiver>()
                        .eq(SysMessageReceiver::getMessageId, messageId)
                        .eq(SysMessageReceiver::getReceiverId, userId)
                        .last("LIMIT 1"));
        SysMessageReceiver receiver = receivers.isEmpty() ? null : receivers.get(0);

        if (receiver != null) {
            // 更新已有记录
            if (receiver.getIsRead() == 0) {
                receiver.setIsRead(1);
                receiver.setReadTime(LocalDateTime.now());
                receiverMapper.updateById(receiver);
            }
        } else {
            // 可能是全员公告，插入已读记录
            SysMessage message = this.getById(messageId);
            if (message != null && "ALL".equals(message.getSendScope())) {
                SysMessageReceiver newReceiver = SysMessageReceiver.builder()
                        .messageId(messageId)
                        .receiverId(userId)
                        .isRead(1)
                        .readTime(LocalDateTime.now())
                        .build();
                receiverMapper.insert(newReceiver);
            }
        }
    }

    /**
     * 标记所有未读全员公告为已读
     */
    private void markBroadcastAsRead(Long userId) {
        // 查找所有未读的全员公告（即用户没有 receiver 记录的 ALL 消息）
        List<SysMessage> broadcasts = this.list(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getSendScope, "ALL")
                        .eq(SysMessage::getDelFlag, 0));

        for (SysMessage broadcast : broadcasts) {
            List<SysMessageReceiver> existingList = receiverMapper.selectList(
                    new LambdaQueryWrapper<SysMessageReceiver>()
                            .eq(SysMessageReceiver::getMessageId, broadcast.getMessageId())
                            .eq(SysMessageReceiver::getReceiverId, userId)
                            .last("LIMIT 1"));
            SysMessageReceiver existing = existingList.isEmpty() ? null : existingList.get(0);
            if (existing == null) {
                SysMessageReceiver receiver = SysMessageReceiver.builder()
                        .messageId(broadcast.getMessageId())
                        .receiverId(userId)
                        .isRead(1)
                        .readTime(LocalDateTime.now())
                        .build();
                receiverMapper.insert(receiver);
            }
        }
    }
}

package com.sc.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.common.core.domain.PageResult;
import com.sc.message.domain.dto.MessageSendDTO;
import com.sc.message.domain.entity.SysMessage;
import com.sc.message.domain.query.MessageQuery;
import com.sc.message.domain.vo.MessageUnreadVO;
import com.sc.message.domain.vo.MessageVO;

import java.util.List;

/**
 * 站内信服务接口
 */
public interface ISysMessageService extends IService<SysMessage> {

    /**
     * 管理侧：发送消息
     *
     * @param dto 发送请求
     */
    void sendMessage(MessageSendDTO dto);

    /**
     * 用户侧：我的消息列表
     *
     * @param userId 当前用户ID
     * @param query  查询参数
     * @return 分页结果
     */
    PageResult<MessageVO> listMyMessages(Long userId, MessageQuery query);

    /**
     * 用户侧：未读数统计
     *
     * @param userId 当前用户ID
     * @return 未读数统计
     */
    MessageUnreadVO countUnread(Long userId);

    /**
     * 用户侧：消息详情（同时标记已读）
     *
     * @param userId    当前用户ID
     * @param messageId 消息ID
     * @return 消息详情
     */
    MessageVO getMessageDetail(Long userId, Long messageId);

    /**
     * 用户侧：批量标记已读
     *
     * @param userId     当前用户ID
     * @param messageIds 消息ID列表
     */
    void markAsRead(Long userId, List<Long> messageIds);

    /**
     * 用户侧：全部标记已读
     *
     * @param userId 当前用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 内部接口：通知模块调用入库
     *
     * @param title       标题
     * @param content     内容
     * @param priority    优先级
     * @param receiverIds 接收者ID列表
     */
    void internalSend(String title, String content, Integer priority, List<Long> receiverIds);
}

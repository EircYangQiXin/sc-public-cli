package com.sc.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sc.message.domain.entity.SysMessageReceiver;
import com.sc.message.domain.query.MessageQuery;
import com.sc.message.domain.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信接收人 Mapper
 */
@Mapper
public interface SysMessageReceiverMapper extends BaseMapper<SysMessageReceiver> {

    /**
     * 查询用户消息列表（包含全员公告）
     *
     * @param userId 用户ID
     * @param query  查询参数
     * @param offset 分页偏移
     * @return 消息列表
     */
    List<MessageVO> selectUserMessages(@Param("userId") Long userId,
                                       @Param("query") MessageQuery query,
                                       @Param("offset") int offset);

    /**
     * 统计用户消息总数（包含全员公告）
     *
     * @param userId 用户ID
     * @param query  查询参数
     * @return 总数
     */
    long countUserMessages(@Param("userId") Long userId,
                           @Param("query") MessageQuery query);

    /**
     * 统计用户各类型未读数
     *
     * @param userId 用户ID
     * @param msgType 消息类型（null 则不过滤类型）
     * @return 未读数
     */
    int countUnreadByType(@Param("userId") Long userId,
                          @Param("msgType") Integer msgType);

    /**
     * 统计用户全员公告未读数
     *
     * @param userId  用户ID
     * @param msgType 消息类型（null 则不过滤类型）
     * @return 未读数
     */
    int countBroadcastUnread(@Param("userId") Long userId,
                             @Param("msgType") Integer msgType);

    /**
     * 查询消息详情（含已读状态）
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return 消息详情
     */
    MessageVO selectMessageDetail(@Param("userId") Long userId,
                                  @Param("messageId") Long messageId);
}

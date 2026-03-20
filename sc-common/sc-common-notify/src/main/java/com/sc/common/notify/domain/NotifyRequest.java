package com.sc.common.notify.domain;

import com.sc.common.notify.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统一通知请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRequest {

    /**
     * 模板编码（可选，如果直接提供 content 则忽略）
     */
    private String templateCode;

    /**
     * 接收人列表（userId / 手机号 / 邮箱 / Webhook URL，取决于渠道类型）
     */
    private List<String> receivers;

    /**
     * 模板变量
     */
    private Map<String, String> params;

    /**
     * 通知渠道
     */
    private ChannelType channelType;

    /**
     * 通知标题（邮件主题 / 站内信标题）
     */
    private String title;

    /**
     * 通知内容（直接内容，优先于模板）
     */
    private String content;

    /**
     * 优先级：0=普通 1=重要 2=紧急
     */
    @Builder.Default
    private Integer priority = 0;
}

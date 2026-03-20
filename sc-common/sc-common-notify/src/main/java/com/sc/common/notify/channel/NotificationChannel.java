package com.sc.common.notify.channel;

import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;

/**
 * 通知渠道策略接口
 * <p>
 * 各渠道（邮件、短信、站内信、Webhook）实现此接口，
 * 由 {@link com.sc.common.notify.service.NotificationService} 根据渠道类型自动路由。
 * </p>
 */
public interface NotificationChannel {

    /**
     * 获取渠道类型
     */
    ChannelType getChannelType();

    /**
     * 发送通知
     *
     * @param request 统一通知请求
     * @return 发送结果
     */
    NotifyResult send(NotifyRequest request);
}

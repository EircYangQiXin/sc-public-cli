package com.sc.common.notify.channel;

import com.sc.common.notify.annotation.DefaultChannel;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;

/**
 * 站内信通知渠道
 * <p>
 * 默认实现仅做日志记录，实际入库逻辑由业务服务（sc-message）通过
 * 覆盖 {@link #doSave} 方法或监听 Spring Event 来实现。
 * </p>
 */
@Slf4j
@DefaultChannel
public class InAppChannel implements NotificationChannel {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.IN_APP;
    }

    @Override
    public NotifyResult send(NotifyRequest request) {
        return doSave(request);
    }

    /**
     * 站内信持久化（子类覆盖此方法实现入库 + WebSocket 推送）
     */
    protected NotifyResult doSave(NotifyRequest request) {
        log.info("站内信默认处理 → receivers={}, title={}", request.getReceivers(), request.getTitle());
        return NotifyResult.ok("站内信已接收（待业务层处理）");
    }
}


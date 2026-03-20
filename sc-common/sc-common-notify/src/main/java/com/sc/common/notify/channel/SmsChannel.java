package com.sc.common.notify.channel;

import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信通知渠道（SPI 扩展点）
 * <p>
 * 当前为预留实现，业务方可通过以下方式接入具体短信服务商：
 * <ol>
 *   <li>继承此类并重写 {@link #doSend} 方法，将子类注册为 Spring Bean 即可替换默认实现</li>
 * </ol>
 * 不建议直接实现 {@link NotificationChannel} 接口返回 {@code ChannelType.SMS}，
 * 这样会导致容器中存在两个 SMS 渠道 Bean、路由取决于 Bean 加载顺序。
 * </p>
 */
@Slf4j
public class SmsChannel implements NotificationChannel {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public NotifyResult send(NotifyRequest request) {
        return doSend(request);
    }

    /**
     * 实际发送短信（子类覆盖此方法对接具体短信服务商）
     * <p>
     * 默认实现仅打印日志，不实际发送。
     * </p>
     */
    protected NotifyResult doSend(NotifyRequest request) {
        log.warn("短信渠道暂未对接具体服务商，跳过发送 → receivers={}", request.getReceivers());
        return NotifyResult.fail("短信渠道暂未配置");
    }
}

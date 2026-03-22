package com.sc.api.message.factory;

import com.sc.api.message.RemoteMessageService;
import com.sc.api.message.dto.InAppMessageDTO;
import com.sc.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 站内信远程调用降级处理
 */
@Slf4j
@Component
public class RemoteMessageFallbackFactory implements FallbackFactory<RemoteMessageService> {

    @Override
    public RemoteMessageService create(Throwable cause) {
        log.error("站内信服务调用失败: {}", cause.getMessage());
        return new RemoteMessageService() {
            @Override
            public R<Void> sendInAppMessage(InAppMessageDTO dto) {
                return R.fail("发送站内信失败: " + cause.getMessage());
            }
        };
    }
}

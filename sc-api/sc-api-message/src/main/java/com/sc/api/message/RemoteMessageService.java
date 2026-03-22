package com.sc.api.message;

import com.sc.api.message.dto.InAppMessageDTO;
import com.sc.api.message.factory.RemoteMessageFallbackFactory;
import com.sc.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 站内信远程调用接口
 */
@FeignClient(value = "sc-message", fallbackFactory = RemoteMessageFallbackFactory.class)
public interface RemoteMessageService {

    /**
     * 发送站内信（内部调用）
     */
    @PostMapping("/message/internal/send")
    R<Void> sendInAppMessage(@RequestBody InAppMessageDTO dto);
}

package com.sc.message.controller;

import cn.dev33.satoken.same.SaSameUtil;
import com.sc.api.message.dto.InAppMessageDTO;
import com.sc.common.core.domain.R;
import com.sc.message.service.ISysMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内信内部接口（Feign 调用）
 * <p>
 * 使用 Sa-Token Same-Token 机制做服务间鉴权。
 * {@link com.sc.common.security.feign.FeignTokenInterceptor} 在 Feign 调用时自动透传 Same-Token，
 * 服务端通过 {@link SaSameUtil#checkCurrentRequestToken()} 校验，
 * 无法被外部伪造（Same-Token 基于密钥动态生成，不是固定明文）。
 * </p>
 */
@Api(tags = "站内信-内部接口")
@RestController
@RequestMapping("/message/internal")
@RequiredArgsConstructor
public class SysMessageInternalController {

    private final ISysMessageService messageService;

    @ApiOperation("内部发送站内信")
    @PostMapping("/send")
    public R<Void> send(@Validated @RequestBody InAppMessageDTO dto) {
        // Sa-Token Same-Token 校验，校验失败自动抛异常
        SaSameUtil.checkCurrentRequestToken();

        messageService.internalSend(dto.getTitle(), dto.getContent(), dto.getPriority(), dto.getReceiverIds());
        return R.ok();
    }
}


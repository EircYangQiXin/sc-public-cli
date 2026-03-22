package com.sc.message.controller;

import com.sc.api.message.dto.InAppMessageDTO;
import com.sc.common.core.domain.R;
import com.sc.message.service.ISysMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 站内信内部接口（Feign 调用，需 X-SC-Internal 签名校验）
 * <p>
 * 虽然路径在 ignore-urls 中免除 Sa-Token 登录校验，但通过
 * 内部调用标识头 {@code X-SC-Internal} 阻止外部伪造请求。
 * 网关应配置剥离外部请求携带的该 Header。
 * </p>
 */
@Api(tags = "站内信-内部接口")
@RestController
@RequestMapping("/message/internal")
@RequiredArgsConstructor
public class SysMessageInternalController {

    private static final String INTERNAL_HEADER = "X-SC-Internal";
    private static final String INTERNAL_SECRET = "sc-internal-feign";

    private final ISysMessageService messageService;
    private final HttpServletRequest request;

    @ApiOperation("内部发送站内信")
    @PostMapping("/send")
    public R<Void> send(@RequestBody InAppMessageDTO dto) {
        // 校验内部调用标识，阻止外部直连伪造
        String internalToken = request.getHeader(INTERNAL_HEADER);
        if (!INTERNAL_SECRET.equals(internalToken)) {
            return R.fail("非法访问");
        }

        messageService.internalSend(dto.getTitle(), dto.getContent(), dto.getPriority(), dto.getReceiverIds());
        return R.ok();
    }
}

package com.sc.message.channel;

import com.sc.common.notify.channel.InAppChannel;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.message.service.ISysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 站内信渠道真实实现
 * <p>
 * 继承默认 {@link InAppChannel}，在 sc-message 模块中注册为 Bean。
 * 由于 {@link InAppChannel} 通过 {@code @ConditionalOnMissingBean} 注册，
 * 本类的存在会自动阻止默认占位 Bean 的创建。
 * 同时 {@link InAppChannel} 标注了 {@code @DefaultChannel}，
 * {@link com.sc.common.notify.service.NotificationService} 会优先选用本类（非默认实现）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppChannelImpl extends InAppChannel {

    private final ISysMessageService messageService;

    @Override
    protected NotifyResult doSave(NotifyRequest request) {
        try {
            List<Long> userIds = parseReceiverIds(request.getReceivers());
            if (userIds.isEmpty()) {
                log.warn("站内信入库失败：接收人列表为空, title={}", request.getTitle());
                return NotifyResult.fail("接收人列表为空");
            }

            messageService.internalSend(
                    request.getTitle(),
                    request.getContent(),
                    request.getPriority(),
                    userIds);

            log.info("站内信已通过 InAppChannelImpl 入库: receivers={}, title={}",
                    userIds.size(), request.getTitle());
            return NotifyResult.ok("站内信已入库");
        } catch (Exception e) {
            log.error("站内信入库异常: {}", e.getMessage(), e);
            return NotifyResult.fail("站内信入库失败: " + e.getMessage());
        }
    }

    /**
     * 将字符串形式的 receivers 转为 userId 列表
     */
    private List<Long> parseReceiverIds(List<String> receivers) {
        if (receivers == null || receivers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = new ArrayList<Long>(receivers.size());
        for (String receiver : receivers) {
            try {
                userIds.add(Long.parseLong(receiver.trim()));
            } catch (NumberFormatException e) {
                log.warn("无法解析 receiverId: {}", receiver);
            }
        }
        return userIds;
    }
}

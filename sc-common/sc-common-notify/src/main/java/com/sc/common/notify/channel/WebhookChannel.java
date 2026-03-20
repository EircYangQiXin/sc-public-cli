package com.sc.common.notify.channel;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Webhook 通知渠道
 * <p>
 * 将通知内容以 JSON POST 方式发送到指定的 Webhook URL。
 * receivers 中存放的是 Webhook URL 列表。
 * </p>
 */
@Slf4j
@Component
public class WebhookChannel implements NotificationChannel {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.WEBHOOK;
    }

    @Override
    public NotifyResult send(NotifyRequest request) {
        try {
            for (String webhookUrl : request.getReceivers()) {
                String payload = buildPayload(request);
                HttpResponse response = HttpRequest.post(webhookUrl)
                        .body(payload, "application/json")
                        .timeout(10000)
                        .execute();
                log.debug("Webhook 发送完成 → url={}, status={}", webhookUrl, response.getStatus());
            }
            return NotifyResult.ok();
        } catch (Exception e) {
            log.error("Webhook 发送失败", e);
            return NotifyResult.fail("Webhook 发送失败: " + e.getMessage());
        }
    }

    /**
     * 构建 Webhook 请求体（可覆盖自定义格式）
     */
    protected String buildPayload(NotifyRequest request) {
        // 默认构建简单 JSON 格式
        return "{\"title\":\"" + escapeJson(request.getTitle())
                + "\",\"content\":\"" + escapeJson(request.getContent())
                + "\",\"priority\":" + request.getPriority() + "}";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

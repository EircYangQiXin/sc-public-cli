package com.sc.common.notify.service;

import com.sc.common.notify.channel.NotificationChannel;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import com.sc.common.notify.template.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 统一通知服务
 * <p>
 * 入口类，业务方通过此服务发送各类通知。
 * 自动收集所有 {@link NotificationChannel} 实现，根据渠道类型路由到对应的 Channel。
 * </p>
 *
 * <pre>
 * &#64;Autowired
 * private NotificationService notificationService;
 *
 * notificationService.send(NotifyRequest.builder()
 *     .channelType(ChannelType.EMAIL)
 *     .title("测试邮件")
 *     .content("这是一封测试邮件")
 *     .receivers(Arrays.asList("test@example.com"))
 *     .build());
 * </pre>
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired(required = false)
    private List<NotificationChannel> channels;

    private final Map<ChannelType, NotificationChannel> channelMap = new EnumMap<>(ChannelType.class);

    @PostConstruct
    public void init() {
        if (channels != null) {
            for (NotificationChannel channel : channels) {
                NotificationChannel existing = channelMap.put(channel.getChannelType(), channel);
                if (existing != null) {
                    log.warn("通知渠道类型 {} 存在多个实现，{} 已被 {} 覆盖",
                            channel.getChannelType(),
                            existing.getClass().getSimpleName(),
                            channel.getClass().getSimpleName());
                }
                log.info("注册通知渠道: {} → {}", channel.getChannelType(), channel.getClass().getSimpleName());
            }
        }
        log.info("通知服务初始化完成，已注册 {} 个渠道", channelMap.size());
    }

    /**
     * 发送通知
     *
     * @param request 通知请求
     * @return 发送结果
     */
    public NotifyResult send(NotifyRequest request) {
        // 渠道路由
        NotificationChannel channel = channelMap.get(request.getChannelType());
        if (channel == null) {
            String msg = "不支持的通知渠道: " + request.getChannelType();
            log.warn(msg);
            return NotifyResult.fail(msg);
        }

        // 模板变量替换
        if (request.getContent() != null && request.getParams() != null && !request.getParams().isEmpty()) {
            request.setContent(TemplateEngine.render(request.getContent(), request.getParams()));
        }
        if (request.getTitle() != null && request.getParams() != null && !request.getParams().isEmpty()) {
            request.setTitle(TemplateEngine.render(request.getTitle(), request.getParams()));
        }

        log.info("发送通知 → channel={}, receivers={}, title={}",
                request.getChannelType(), request.getReceivers(), request.getTitle());

        return channel.send(request);
    }

    /**
     * 批量发送通知（发送到多个渠道）
     *
     * @param requests 通知请求列表
     * @return 每个渠道的发送结果
     */
    public Map<ChannelType, NotifyResult> sendBatch(List<NotifyRequest> requests) {
        Map<ChannelType, NotifyResult> results = new EnumMap<>(ChannelType.class);
        for (NotifyRequest request : requests) {
            results.put(request.getChannelType(), send(request));
        }
        return results;
    }

    /**
     * 检查指定渠道是否可用
     */
    public boolean isChannelAvailable(ChannelType channelType) {
        return channelMap.containsKey(channelType);
    }
}

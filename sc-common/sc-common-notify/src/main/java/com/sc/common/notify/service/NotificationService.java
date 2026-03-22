package com.sc.common.notify.service;

import com.sc.common.notify.annotation.DefaultChannel;
import com.sc.common.notify.channel.NotificationChannel;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import com.sc.common.notify.template.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一通知服务
 * <p>
 * 入口类，业务方通过此服务发送各类通知。
 * 自动收集所有 {@link NotificationChannel} 实现，根据渠道类型路由到对应的 Channel。
 * </p>
 * <p>
 * <b>渠道冲突解决机制：</b>当同一 {@link ChannelType} 存在多个实现时，
 * 按以下规则选取唯一路由目标：
 * <ol>
 *   <li>标注了 {@link DefaultChannel @DefaultChannel} 的实现优先级最低（留作兜底）</li>
 *   <li>未标注 {@code @DefaultChannel} 的实现优先级更高（用户自定义渠道自动替换默认渠道）</li>
 *   <li>同级别内再按 {@link org.springframework.core.annotation.Order @Order} 排序（值越小优先级越高）</li>
 * </ol>
 * 因此，用户自定义渠道无需额外配置即可自动替换默认渠道。
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
        if (channels == null || channels.isEmpty()) {
            log.info("通知服务初始化完成，未发现任何通知渠道实现");
            return;
        }

        // 按 ChannelType 分组
        Map<ChannelType, List<NotificationChannel>> grouped = new HashMap<ChannelType, List<NotificationChannel>>();
        for (NotificationChannel channel : channels) {
            List<NotificationChannel> list = grouped.get(channel.getChannelType());
            if (list == null) {
                list = new ArrayList<NotificationChannel>();
                grouped.put(channel.getChannelType(), list);
            }
            list.add(channel);
        }

        // 自定义比较器：非 @DefaultChannel 优先，同级别再按 @Order 排序
        Comparator<NotificationChannel> channelComparator = new Comparator<NotificationChannel>() {
            @Override
            public int compare(NotificationChannel a, NotificationChannel b) {
                boolean aDefault = isDefaultChannel(a);
                boolean bDefault = isDefaultChannel(b);
                if (aDefault != bDefault) {
                    // 非默认在前（false < true → 返回 -1 让非默认排前面）
                    return aDefault ? 1 : -1;
                }
                // 同级别按 @Order 排序
                return AnnotationAwareOrderComparator.INSTANCE.compare(a, b);
            }
        };

        for (Map.Entry<ChannelType, List<NotificationChannel>> entry : grouped.entrySet()) {
            ChannelType type = entry.getKey();
            List<NotificationChannel> impls = entry.getValue();

            // 排序：非默认 + 低 @Order 的排在最前
            Collections.sort(impls, channelComparator);
            NotificationChannel selected = impls.get(0);
            channelMap.put(type, selected);

            if (impls.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < impls.size(); i++) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(impls.get(i).getClass().getSimpleName());
                    if (isDefaultChannel(impls.get(i))) {
                        sb.append("(默认)");
                    }
                }
                log.warn("通知渠道 {} 存在 {} 个实现，选用 {}，忽略: [{}]",
                        type, impls.size(), selected.getClass().getSimpleName(), sb.toString());
            }

            log.info("注册通知渠道: {} → {}", type, selected.getClass().getSimpleName());
        }

        log.info("通知服务初始化完成，已注册 {} 个渠道", channelMap.size());
    }

    /**
     * 判断渠道实现是否<b>直接</b>标注了 {@link DefaultChannel} 注解。
     * <p>
     * 使用 {@link Class#isAnnotationPresent} 而非 {@code AnnotationUtils.findAnnotation}，
     * 因为后者会沿父类继承链查找，导致 {@code extends SmsChannel} 的用户子类被误判为默认渠道。
     * {@code @DefaultChannel} 未标注 {@code @Inherited}，所以 {@code isAnnotationPresent}
     * 只检查类自身声明的注解。
     * </p>
     */
    private boolean isDefaultChannel(NotificationChannel channel) {
        return channel.getClass().isAnnotationPresent(DefaultChannel.class);
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



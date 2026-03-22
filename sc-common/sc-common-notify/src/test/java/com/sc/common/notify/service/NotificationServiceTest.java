package com.sc.common.notify.service;

import com.sc.common.notify.annotation.DefaultChannel;
import com.sc.common.notify.channel.NotificationChannel;
import com.sc.common.notify.channel.SmsChannel;
import com.sc.common.notify.domain.NotifyRequest;
import com.sc.common.notify.domain.NotifyResult;
import com.sc.common.notify.enums.ChannelType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.Order;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link NotificationService} 渠道冲突解决逻辑的单元测试。
 * <p>
 * 不启动 Spring 上下文，通过反射注入 channels 字段，直接测试 init() 方法。
 * </p>
 */
public class NotificationServiceTest {

    private NotificationService service;

    @Before
    public void setUp() {
        service = new NotificationService();
    }

    // ======================= 辅助 Channel 实现 =======================

    /** 模拟"直接实现接口"的用户自定义 SMS 渠道（不带 @DefaultChannel） */
    static class CustomSmsChannel implements NotificationChannel {
        @Override
        public ChannelType getChannelType() {
            return ChannelType.SMS;
        }

        @Override
        public NotifyResult send(NotifyRequest request) {
            return NotifyResult.ok("custom-sms");
        }
    }

    /** 模拟"继承 SmsChannel"的用户自定义渠道（不带 @DefaultChannel） */
    static class ExtendedSmsChannel extends SmsChannel {
        @Override
        protected NotifyResult doSend(NotifyRequest request) {
            return NotifyResult.ok("extended-sms");
        }
    }

    /** 另一个不带 @DefaultChannel 的自定义 SMS 渠道，但带 @Order(1) */
    @Order(1)
    static class HighPriorityCustomSmsChannel implements NotificationChannel {
        @Override
        public ChannelType getChannelType() {
            return ChannelType.SMS;
        }

        @Override
        public NotifyResult send(NotifyRequest request) {
            return NotifyResult.ok("high-priority-sms");
        }
    }

    /** 另一个不带 @DefaultChannel 的自定义 SMS 渠道，带 @Order(100) */
    @Order(100)
    static class LowPriorityCustomSmsChannel implements NotificationChannel {
        @Override
        public ChannelType getChannelType() {
            return ChannelType.SMS;
        }

        @Override
        public NotifyResult send(NotifyRequest request) {
            return NotifyResult.ok("low-priority-sms");
        }
    }

    /** 带 @DefaultChannel 的额外默认渠道（测试两个默认渠道并存场景） */
    @DefaultChannel
    static class AnotherDefaultSmsChannel implements NotificationChannel {
        @Override
        public ChannelType getChannelType() {
            return ChannelType.SMS;
        }

        @Override
        public NotifyResult send(NotifyRequest request) {
            return NotifyResult.ok("another-default-sms");
        }
    }

    // ======================= 测试用例 =======================

    /**
     * 场景1：只有默认短信渠道，正常注册
     */
    @Test
    public void testOnlyDefaultChannel() {
        SmsChannel defaultSms = new SmsChannel();
        ReflectionTestUtils.setField(service, "channels",
                Collections.<NotificationChannel>singletonList(defaultSms));

        service.init();

        assertTrue(service.isChannelAvailable(ChannelType.SMS));
    }

    /**
     * 场景2：直接实现 NotificationChannel 的用户自定义 SMS 渠道 + 默认 SmsChannel。
     * 预期：选用 CustomSmsChannel（不带 @DefaultChannel 的优先）
     */
    @Test
    public void testCustomChannelReplacesDefault() {
        SmsChannel defaultSms = new SmsChannel();
        CustomSmsChannel customSms = new CustomSmsChannel();

        // 故意将默认放在前面，验证不受注入顺序影响
        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(defaultSms, customSms));

        service.init();

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("custom-sms", result.getMessage());
    }

    /**
     * 场景3：反转注入顺序——用户自定义在前，默认在后。
     * 预期：仍然选用 CustomSmsChannel
     */
    @Test
    public void testCustomChannelReplacesDefault_reversedOrder() {
        SmsChannel defaultSms = new SmsChannel();
        CustomSmsChannel customSms = new CustomSmsChannel();

        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(customSms, defaultSms));

        service.init();

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertEquals("custom-sms", result.getMessage());
    }

    /**
     * 场景4：继承 SmsChannel 的子类 + 默认 SmsChannel。
     * <p>
     * ExtendedSmsChannel 继承自 SmsChannel，SmsChannel 上标了 @DefaultChannel，
     * 但 @DefaultChannel 没有 @Inherited 元注解，所以 ExtendedSmsChannel 不会被
     * isAnnotationPresent 判定为默认渠道 → 它是用户自定义渠道，优先于默认 SmsChannel。
     * </p>
     * <p>
     * 注意：实际使用中继承方式会触发 @ConditionalOnMissingBean(SmsChannel.class)，
     * 默认 SmsChannel 不会被注册。本场景测试的是"即使两个都被注册了"的极端情况。
     * </p>
     */
    @Test
    public void testExtendedChannelReplacesDefault() {
        SmsChannel defaultSms = new SmsChannel();
        ExtendedSmsChannel extendedSms = new ExtendedSmsChannel();

        // 默认在前，验证不受注入顺序影响
        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(defaultSms, extendedSms));

        service.init();

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertTrue(result.isSuccess());
        assertEquals("extended-sms", result.getMessage());
    }

    /**
     * 场景5：两个非默认 SMS 渠道 + 不同 @Order 值。
     * 预期：选用 @Order(1) 的 HighPriorityCustomSmsChannel
     */
    @Test
    public void testMultipleCustomChannels_differentOrder() {
        HighPriorityCustomSmsChannel highPriority = new HighPriorityCustomSmsChannel();
        LowPriorityCustomSmsChannel lowPriority = new LowPriorityCustomSmsChannel();

        // 故意低优先级在前
        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(lowPriority, highPriority));

        service.init();

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertEquals("high-priority-sms", result.getMessage());
    }

    /**
     * 场景6：两个非默认 SMS 渠道 + 缺省 @Order（相同优先级）。
     * 预期：不抛异常，选其中一个
     */
    @Test
    public void testMultipleCustomChannels_sameOrder_noException() {
        CustomSmsChannel custom1 = new CustomSmsChannel();
        CustomSmsChannel custom2 = new CustomSmsChannel();

        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(custom1, custom2));

        // 不应抛异常
        service.init();

        assertTrue(service.isChannelAvailable(ChannelType.SMS));
    }

    /**
     * 场景7：两个非默认 SMS + 一个默认 SmsChannel。
     * 预期：选用 @Order(1) 的非默认，忽略其余
     */
    @Test
    public void testMixedChannels_customWinsOverDefault() {
        SmsChannel defaultSms = new SmsChannel();
        HighPriorityCustomSmsChannel highPriority = new HighPriorityCustomSmsChannel();
        LowPriorityCustomSmsChannel lowPriority = new LowPriorityCustomSmsChannel();

        ReflectionTestUtils.setField(service, "channels",
                Arrays.<NotificationChannel>asList(defaultSms, lowPriority, highPriority));

        service.init();

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertEquals("high-priority-sms", result.getMessage());
    }

    /**
     * 场景8：无渠道实现。
     * 预期：init 不抛异常，发送时返回失败
     */
    @Test
    public void testNoChannels() {
        ReflectionTestUtils.setField(service, "channels", null);
        service.init();

        assertFalse(service.isChannelAvailable(ChannelType.SMS));

        NotifyRequest request = NotifyRequest.builder()
                .channelType(ChannelType.SMS)
                .title("test")
                .content("test")
                .receivers(Collections.singletonList("13800138000"))
                .build();

        NotifyResult result = service.send(request);
        assertFalse(result.isSuccess());
    }
}

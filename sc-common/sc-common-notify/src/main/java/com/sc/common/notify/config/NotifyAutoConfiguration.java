package com.sc.common.notify.config;

import com.sc.common.notify.channel.InAppChannel;
import com.sc.common.notify.channel.SmsChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 通知模块自动配置
 * <p>
 * 自动注册默认渠道实现。业务方可通过注册同类型的 Bean 替换默认实现。
 * </p>
 */
@Configuration
@ComponentScan(basePackages = {
        "com.sc.common.notify.channel",
        "com.sc.common.notify.service"
})
public class NotifyAutoConfiguration {

    /**
     * 默认短信渠道（SPI 扩展点，仅日志输出）
     * <p>
     * 业务方可通过以下方式替换：
     * <ol>
     *   <li><b>推荐：</b>继承 {@link SmsChannel} 并注册为 Bean，
     *       {@code @ConditionalOnMissingBean(SmsChannel.class)} 会阻止此默认 Bean 创建</li>
     *   <li>直接实现 {@link com.sc.common.notify.channel.NotificationChannel} 接口，
     *       {@code getChannelType()} 返回 {@code ChannelType.SMS}，注册为 Bean。
     *       {@link com.sc.common.notify.service.NotificationService} 会识别默认实现上的
     *       {@link com.sc.common.notify.annotation.DefaultChannel @DefaultChannel} 标记，
     *       始终优先选用用户自定义实现</li>
     * </ol>
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(SmsChannel.class)
    public SmsChannel smsChannel() {
        return new SmsChannel();
    }

    /**
     * 默认站内信渠道
     */
    @Bean
    @ConditionalOnMissingBean(InAppChannel.class)
    public InAppChannel inAppChannel() {
        return new InAppChannel();
    }
}


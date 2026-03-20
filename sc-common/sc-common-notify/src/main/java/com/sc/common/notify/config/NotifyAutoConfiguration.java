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
     *   <li>继承 {@link SmsChannel} 并注册为 Bean（覆盖此默认 Bean）</li>
     *   <li>实现 {@link com.sc.common.notify.channel.NotificationChannel} 接口，
     *       {@code getChannelType()} 返回 {@code ChannelType.SMS}，
     *       并注册为 Bean + 标注 {@code @Primary}
     *       （此时默认 SmsChannel 仍有效，但 {@code @Primary} 实现优先）</li>
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

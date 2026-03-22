package com.sc.common.notify.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记默认/占位渠道实现
 * <p>
 * 当容器中同一 {@link com.sc.common.notify.enums.ChannelType} 存在多个
 * {@link com.sc.common.notify.channel.NotificationChannel} 实现时，
 * {@link com.sc.common.notify.service.NotificationService} 会<b>始终优先选用
 * 未标注此注解的实现</b>，从而确保用户自定义渠道自动替换默认占位渠道。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultChannel {
}

package com.sc.auth.captcha;

import java.util.Map;

/**
 * 验证码策略接口
 * <p>
 * 设计模式：策略模式
 * 不同验证码类型实现此接口，通过 Spring 依赖注入切换策略
 * 可扩展实现：图形验证码、滑动验证码、短信验证码等
 * </p>
 */
public interface CaptchaStrategy {

    /**
     * 生成验证码
     *
     * @return 包含 uuid 和 image(base64) 的 Map
     */
    Map<String, Object> generate();

    /**
     * 校验验证码
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 是否校验通过
     */
    boolean validate(String uuid, String code);
}

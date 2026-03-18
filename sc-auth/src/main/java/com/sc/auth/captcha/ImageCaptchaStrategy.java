package com.sc.auth.captcha;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码策略（基于 Google Kaptcha）
 * <p>
 * 策略模式默认实现：生成图片验证码，Base64 返回给前端
 * 验证码存储在 Redis 中，有效期 5 分钟
 * </p>
 */
@Slf4j
@Component
public class ImageCaptchaStrategy implements CaptchaStrategy {

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final Producer kaptchaProducer;

    public ImageCaptchaStrategy(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.kaptchaProducer = createProducer();
    }

    @Override
    public Map<String, Object> generate() {
        // 生成验证码文本
        String code = kaptchaProducer.createText();
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis
        redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + uuid,
                code,
                CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 生成图片并转 Base64
        BufferedImage image = kaptchaProducer.createImage(code);
        String base64;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("验证码图片生成失败", e);
            throw new RuntimeException("验证码生成失败");
        }

        Map<String, Object> result = new HashMap<>(4);
        result.put("uuid", uuid);
        result.put("image", base64);
        return result;
    }

    @Override
    public boolean validate(String uuid, String code) {
        if (uuid == null || code == null) {
            return false;
        }
        String key = CAPTCHA_KEY_PREFIX + uuid;
        String cachedCode = redisTemplate.opsForValue().get(key);
        // 验证后立即删除（一次性使用）
        redisTemplate.delete(key);
        return code.equalsIgnoreCase(cachedCode);
    }

    /**
     * 创建 Kaptcha 生产者（配置样式）
     */
    private Producer createProducer() {
        Properties properties = new Properties();
        // 图片宽高
        properties.setProperty("kaptcha.image.width", "200");
        properties.setProperty("kaptcha.image.height", "60");
        // 字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 字符范围（排除易混淆字符 0OoIl1）
        properties.setProperty("kaptcha.textproducer.char.string", "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.size", "38");
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Verdana,Tahoma");
        // 干扰线
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        // 背景渐变
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        // 边框
        properties.setProperty("kaptcha.border", "no");

        Config config = new Config(properties);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);
        return kaptcha;
    }
}

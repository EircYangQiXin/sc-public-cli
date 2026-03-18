package com.sc.common.mybatis.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-CBC 加密策略（默认实现）
 * <p>
 * 配置项：
 * <ul>
 *   <li>{@code sc.encrypt.key} — AES 密钥（至少 16 字符，建议 32 字符即 AES-256）</li>
 *   <li>{@code sc.encrypt.iv}  — 初始化向量（16 字符）</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class AesEncryptStrategy implements EncryptStrategy {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public AesEncryptStrategy(
            @Value("${sc.encrypt.key:ScPublicCli@12345678901234}") String key,
            @Value("${sc.encrypt.iv:ScPublic20260318}") String iv) {
        // 确保 key 长度为 16/24/32 字节
        byte[] keyBytes = Arrays.copyOf(key.getBytes(StandardCharsets.UTF_8), 32);
        byte[] ivBytes = Arrays.copyOf(iv.getBytes(StandardCharsets.UTF_8), 16);
        this.keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        this.ivSpec = new IvParameterSpec(ivBytes);
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败，可能是未加密数据或密钥不匹配", e);
            // 如果解密失败，返回原文（兼容历史未加密数据）
            return cipherText;
        }
    }

    @Override
    public String algorithm() {
        return "AES";
    }
}

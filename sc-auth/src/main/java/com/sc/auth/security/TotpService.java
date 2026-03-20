package com.sc.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * TOTP (RFC 6238) 服务 — 纯 JDK 实现
 * <p>
 * 基于 HMAC-SHA1、30 秒时间窗口、6 位数字码。
 * 兼容 Google Authenticator / Microsoft Authenticator 等标准 TOTP 客户端。
 * </p>
 */
@Slf4j
@Service
public class TotpService {

    private static final int SECRET_LENGTH = 20;
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int TOLERANCE_STEPS = 1;
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * 生成随机密钥（Base32 编码）
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_LENGTH];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * 生成 otpauth:// URI（用于生成二维码）
     */
    public String generateQrUri(String secret, String username, String issuer) {
        return "otpauth://totp/" + issuer + ":" + username
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=SHA1"
                + "&digits=" + CODE_DIGITS
                + "&period=" + TIME_STEP_SECONDS;
    }

    /**
     * 验证 TOTP 码（允许前后各 1 个时间窗口的偏差）
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) {
            return false;
        }

        int inputCode;
        try {
            inputCode = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return false;
        }

        long currentStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        byte[] secretBytes = base32Decode(secret);

        for (int i = -TOLERANCE_STEPS; i <= TOLERANCE_STEPS; i++) {
            int expectedCode = generateCode(secretBytes, currentStep + i);
            if (expectedCode == inputCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算指定时间步长的 TOTP 码
     */
    private int generateCode(byte[] secret, long timeStep) {
        try {
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(timeBytes);

            // Dynamic truncation (RFC 4226)
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int mod = 1;
            for (int i = 0; i < CODE_DIGITS; i++) {
                mod *= 10;
            }
            return binary % mod;
        } catch (Exception e) {
            log.error("TOTP 计算失败", e);
            return -1;
        }
    }

    /**
     * Base32 编码
     */
    private String base32Encode(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }

        if (bitsLeft > 0) {
            result.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return result.toString();
    }

    /**
     * Base32 解码
     */
    private byte[] base32Decode(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outLength = encoded.length() * 5 / 8;
        byte[] result = new byte[outLength];

        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (char c : encoded.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;

            buffer = (buffer << 5) | val;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (index < outLength) {
                    result[index++] = (byte) (buffer >> bitsLeft);
                }
            }
        }
        return result;
    }
}

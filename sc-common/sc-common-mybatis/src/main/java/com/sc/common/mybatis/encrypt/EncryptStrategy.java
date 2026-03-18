package com.sc.common.mybatis.encrypt;

/**
 * 加密策略接口
 * <p>
 * 设计模式：策略模式
 * 不同加密算法实现此接口，通过配置或注解属性选择策略
 * </p>
 */
public interface EncryptStrategy {

    /**
     * 加密
     *
     * @param plainText 明文
     * @return 密文
     */
    String encrypt(String plainText);

    /**
     * 解密
     *
     * @param cipherText 密文
     * @return 明文
     */
    String decrypt(String cipherText);

    /**
     * 策略标识
     */
    String algorithm();
}

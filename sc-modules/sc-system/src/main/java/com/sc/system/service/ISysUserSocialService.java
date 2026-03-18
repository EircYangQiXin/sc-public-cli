package com.sc.system.service;

import com.sc.system.domain.entity.SysUserSocial;

import java.util.List;

/**
 * 第三方账号绑定业务接口
 */
public interface ISysUserSocialService {

    /**
     * 查询用户绑定的第三方账号列表
     *
     * @param userId 用户ID
     * @return 绑定列表
     */
    List<SysUserSocial> selectByUserId(Long userId);

    /**
     * 根据第三方平台查询绑定信息
     *
     * @param socialType 平台类型
     * @param socialId   第三方唯一ID
     * @return 绑定信息
     */
    SysUserSocial selectBySocialId(String socialType, String socialId);

    /**
     * 根据 unionId 查询绑定信息（微信专用）
     *
     * @param unionId 微信 unionId
     * @return 绑定信息
     */
    SysUserSocial selectByUnionId(String unionId);

    /**
     * 绑定第三方账号
     *
     * @param social 绑定信息
     */
    void bindSocial(SysUserSocial social);

    /**
     * 解绑第三方账号
     *
     * @param userId     用户ID
     * @param socialType 平台类型
     */
    void unbindSocial(Long userId, String socialType);

    /**
     * 更新 Token 信息
     *
     * @param social 更新的绑定信息
     */
    void updateToken(SysUserSocial social);

    /**
     * 检查用户是否已绑定指定平台
     *
     * @param userId     用户ID
     * @param socialType 平台类型
     * @return 是否已绑定
     */
    boolean isBound(Long userId, String socialType);
}

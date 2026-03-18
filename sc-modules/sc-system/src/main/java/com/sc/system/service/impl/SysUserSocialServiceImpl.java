package com.sc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.common.core.exception.ServiceException;
import com.sc.system.domain.entity.SysUserSocial;
import com.sc.system.mapper.SysUserSocialMapper;
import com.sc.system.service.ISysUserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 第三方账号绑定服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserSocialServiceImpl implements ISysUserSocialService {

    private final SysUserSocialMapper socialMapper;

    @Override
    public List<SysUserSocial> selectByUserId(Long userId) {
        return socialMapper.selectList(
                new LambdaQueryWrapper<SysUserSocial>().eq(SysUserSocial::getUserId, userId));
    }

    @Override
    public SysUserSocial selectBySocialId(String socialType, String socialId) {
        return socialMapper.selectOne(
                new LambdaQueryWrapper<SysUserSocial>()
                        .eq(SysUserSocial::getSocialType, socialType)
                        .eq(SysUserSocial::getSocialId, socialId));
    }

    @Override
    public SysUserSocial selectByUnionId(String unionId) {
        return socialMapper.selectOne(
                new LambdaQueryWrapper<SysUserSocial>()
                        .eq(SysUserSocial::getUnionId, unionId)
                        .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSocial(SysUserSocial social) {
        // 检查是否已绑定
        if (isBound(social.getUserId(), social.getSocialType())) {
            throw new ServiceException("该平台账号已绑定，请先解绑再重新绑定");
        }

        // 检查该第三方账号是否已被其他用户绑定
        SysUserSocial existing = selectBySocialId(social.getSocialType(), social.getSocialId());
        if (existing != null) {
            throw new ServiceException("该第三方账号已被其他用户绑定");
        }

        socialMapper.insert(social);
        log.info("用户 [{}] 绑定第三方账号 [{}:{}]", social.getUserId(), social.getSocialType(), social.getSocialId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindSocial(Long userId, String socialType) {
        int rows = socialMapper.delete(
                new LambdaQueryWrapper<SysUserSocial>()
                        .eq(SysUserSocial::getUserId, userId)
                        .eq(SysUserSocial::getSocialType, socialType));
        if (rows == 0) {
            throw new ServiceException("未找到该绑定关系");
        }
        log.info("用户 [{}] 解绑第三方账号 [{}]", userId, socialType);
    }

    @Override
    public void updateToken(SysUserSocial social) {
        socialMapper.updateById(social);
    }

    @Override
    public boolean isBound(Long userId, String socialType) {
        Long count = socialMapper.selectCount(
                new LambdaQueryWrapper<SysUserSocial>()
                        .eq(SysUserSocial::getUserId, userId)
                        .eq(SysUserSocial::getSocialType, socialType));
        return count != null && count > 0;
    }
}

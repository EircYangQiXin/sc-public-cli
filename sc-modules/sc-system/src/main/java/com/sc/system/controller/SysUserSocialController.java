package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.system.domain.entity.SysUserSocial;
import com.sc.system.domain.vo.SysUserSocialVO;
import com.sc.system.service.ISysUserSocialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方账号绑定控制器
 *
 * <p>
 * 支持的第三方平台：
 * wechat_open（微信开放平台）、wechat_mp（微信公众号）、wechat_mini（微信小程序）、
 * alipay（支付宝）、qq（QQ）、weibo（微博）、github（GitHub）、
 * dingtalk（钉钉）、wechat_work（企业微信）、apple（Apple）、google（Google）
 * </p>
 */
@Api(tags = "第三方账号绑定")
@RestController
@RequestMapping("/system/social")
@RequiredArgsConstructor
public class SysUserSocialController {

    private final ISysUserSocialService socialService;

    @ApiOperation("查询当前用户已绑定的第三方账号")
    @SaCheckLogin
    @GetMapping("/list")
    public R<List<SysUserSocialVO>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SysUserSocial> socials = socialService.selectByUserId(userId);
        List<SysUserSocialVO> voList = socials.stream().map(s -> {
            SysUserSocialVO vo = new SysUserSocialVO();
            vo.setId(s.getId());
            vo.setSocialType(s.getSocialType());
            vo.setSocialNickname(s.getSocialNickname());
            vo.setSocialAvatar(s.getSocialAvatar());
            vo.setCreateTime(s.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return R.ok(voList);
    }

    @ApiOperation("检查当前用户是否已绑定指定平台")
    @SaCheckLogin
    @GetMapping("/check/{socialType}")
    public R<Boolean> checkBound(
            @ApiParam(value = "平台类型 (wechat_open/wechat_mp/wechat_mini/alipay/qq/weibo/github/dingtalk/wechat_work/apple/google)")
            @PathVariable String socialType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(socialService.isBound(userId, socialType));
    }

    @ApiOperation("绑定第三方账号")
    @SaCheckLogin
    @OperationLog(title = "第三方账号绑定", businessType = BusinessType.INSERT)
    @PostMapping("/bind")
    public R<Void> bind(@Valid @RequestBody SysUserSocial social) {
        Long userId = StpUtil.getLoginIdAsLong();
        social.setUserId(userId);
        socialService.bindSocial(social);
        return R.ok("绑定成功", null);
    }

    @ApiOperation("解绑第三方账号")
    @SaCheckLogin
    @OperationLog(title = "第三方账号解绑", businessType = BusinessType.DELETE)
    @DeleteMapping("/unbind/{socialType}")
    public R<Void> unbind(
            @ApiParam(value = "平台类型")
            @PathVariable String socialType) {
        Long userId = StpUtil.getLoginIdAsLong();
        socialService.unbindSocial(userId, socialType);
        return R.ok("解绑成功", null);
    }

    @ApiOperation("通过第三方账号查询绑定用户（内部调用）")
    @GetMapping("/user/{socialType}/{socialId}")
    public R<SysUserSocial> getBySocialId(
            @PathVariable String socialType,
            @PathVariable String socialId) {
        return R.ok(socialService.selectBySocialId(socialType, socialId));
    }
}

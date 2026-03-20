package com.sc.auth.controller;

import com.sc.auth.captcha.CaptchaStrategy;
import com.sc.auth.domain.LoginBody;
import com.sc.auth.domain.vo.CaptchaVO;
import com.sc.auth.domain.vo.LoginTokenVO;
import com.sc.auth.service.LoginService;
import com.sc.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 登录认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final CaptchaStrategy captchaStrategy;

    @ApiOperation("获取图形验证码")
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        CaptchaVO result = captchaStrategy.generate();
        return R.ok(result);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<LoginTokenVO> login(@Valid @RequestBody LoginBody loginBody) {
        // 校验验证码
        if (loginBody.getCode() != null && loginBody.getUuid() != null) {
            if (!captchaStrategy.validate(loginBody.getUuid(), loginBody.getCode())) {
                return R.fail("验证码错误或已过期");
            }
        }

        LoginTokenVO result = loginService.login(
                loginBody.getUsername(),
                loginBody.getPassword(),
                loginBody.getMfaCode(),
                loginBody.getDeviceFingerprint(),
                loginBody.getTrustDevice());
        return R.ok("登录成功", result);
    }

    @ApiOperation("MFA 验证（两步登录第二步）")
    @PostMapping("/mfa/verify")
    public R<LoginTokenVO> verifyMfa(
            @ApiParam(value = "MFA 临时令牌", required = true) @RequestParam String mfaToken,
            @ApiParam(value = "TOTP 验证码", required = true) @RequestParam String mfaCode,
            @ApiParam(value = "设备指纹") @RequestParam(required = false) String deviceFingerprint,
            @ApiParam(value = "是否信任当前设备") @RequestParam(required = false) Boolean trustDevice) {
        LoginTokenVO result = loginService.verifyMfa(mfaToken, mfaCode, deviceFingerprint, trustDevice);
        return R.ok("MFA 验证成功", result);
    }

    @ApiOperation("用户退出")
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功", null);
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public R<LoginTokenVO> refresh() {
        LoginTokenVO result = loginService.refreshToken();
        return R.ok(result);
    }
}

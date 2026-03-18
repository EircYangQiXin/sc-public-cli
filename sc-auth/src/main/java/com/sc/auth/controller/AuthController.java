package com.sc.auth.controller;

import com.sc.auth.captcha.CaptchaStrategy;
import com.sc.auth.domain.LoginBody;
import com.sc.auth.service.LoginService;
import com.sc.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

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
    public R<Map<String, Object>> captcha() {
        Map<String, Object> result = captchaStrategy.generate();
        return R.ok(result);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody loginBody) {
        // 校验验证码
        if (loginBody.getCode() != null && loginBody.getUuid() != null) {
            if (!captchaStrategy.validate(loginBody.getUuid(), loginBody.getCode())) {
                return R.fail("验证码错误或已过期");
            }
        }

        Map<String, Object> result = loginService.login(loginBody.getUsername(), loginBody.getPassword());
        return R.ok("登录成功", result);
    }

    @ApiOperation("用户退出")
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功", null);
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public R<Map<String, Object>> refresh() {
        Map<String, Object> result = loginService.refreshToken();
        return R.ok(result);
    }
}

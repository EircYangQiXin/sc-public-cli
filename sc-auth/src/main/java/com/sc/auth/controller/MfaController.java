package com.sc.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.sc.api.system.RemoteUserService;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.auth.security.SecurityProperties;
import com.sc.auth.security.TotpService;
import com.sc.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * MFA 配置控制器（需登录态）
 */
@Api(tags = "MFA 管理")
@RestController
@RequestMapping("/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final TotpService totpService;
    private final SecurityProperties securityProperties;
    private final RemoteUserService remoteUserService;

    @ApiOperation("生成 MFA 密钥（开始绑定流程）")
    @PostMapping("/setup")
    public R<MfaSetupVO> setup() {
        StpUtil.checkLogin();
        String username = getUsername();

        String secret = totpService.generateSecret();
        String qrUri = totpService.generateQrUri(secret, username, securityProperties.getMfa().getIssuer());

        MfaSetupVO vo = new MfaSetupVO();
        vo.setSecret(secret);
        vo.setQrUri(qrUri);
        return R.ok(vo);
    }

    @ApiOperation("验证并确认启用 MFA")
    @PostMapping("/confirm")
    public R<Void> confirm(@RequestParam String secret, @RequestParam String code) {
        StpUtil.checkLogin();

        // 验证 TOTP 码
        if (!totpService.verifyCode(secret, code)) {
            return R.fail("验证码错误，请重试");
        }

        // 远程调用更新用户 MFA 信息
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserDTO dto = new SysUserDTO();
        dto.setUserId(userId);
        dto.setMfaSecret(secret);
        dto.setMfaEnabled(1);
        remoteUserService.updateUserMfa(dto);

        return R.ok("MFA 已启用", null);
    }

    @ApiOperation("关闭 MFA（需验证当前 TOTP 码）")
    @PostMapping("/disable")
    public R<Void> disable(@RequestParam String code) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取当前 MFA 密钥
        String username = getUsername();
        R<SysUserDTO> result = remoteUserService.getUserInfo(username);
        if (!result.isSuccess() || result.getData() == null) {
            return R.fail("获取用户信息失败");
        }

        SysUserDTO user = result.getData();
        if (user.getMfaSecret() == null || user.getMfaSecret().isEmpty()) {
            return R.fail("MFA 未启用");
        }

        // 验证 TOTP 码
        if (!totpService.verifyCode(user.getMfaSecret(), code)) {
            return R.fail("验证码错误");
        }

        // 关闭 MFA
        SysUserDTO dto = new SysUserDTO();
        dto.setUserId(userId);
        dto.setMfaSecret("");
        dto.setMfaEnabled(0);
        remoteUserService.updateUserMfa(dto);

        return R.ok("MFA 已关闭", null);
    }

    private String getUsername() {
        Object loginUser = StpUtil.getSession().get("loginUser");
        if (loginUser instanceof com.sc.common.core.domain.model.LoginUser) {
            return ((com.sc.common.core.domain.model.LoginUser) loginUser).getUsername();
        }
        return StpUtil.getLoginIdAsString();
    }

    @Data
    @ApiModel(description = "MFA 绑定信息")
    public static class MfaSetupVO {
        @ApiModelProperty(value = "MFA 密钥 (Base32)")
        private String secret;
        @ApiModelProperty(value = "otpauth:// URI (用于生成二维码)")
        private String qrUri;
    }
}

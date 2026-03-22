# sc-api-auth 模块

## 定位
用于封装 `sc-auth` 身份认证服务的 Feign 合约，目前是一个轻量占位模块，后续可以在此处定义登录、注销、刷新、MFA 等远程接口。

## 参考接口（来自 `sc-auth`）
- `GET /auth/captcha`：获取验证码 `CaptchaVO`，用于登录前的图形校验。
- `POST /auth/login`：提交 `LoginBody`（用户名/密码/验证码/MFA 等参数），返回 `LoginTokenVO`。
- `POST /auth/mfa/verify`：针对 MFA 降级场景校验 `mfaToken` 和 `mfaCode`。
- `POST /auth/logout`：退出登录。
- `POST /auth/refresh`：刷新 Token。
- `POST /auth/mfa/setup`、`/auth/mfa/confirm`、`/auth/mfa/disable`：MFA 令牌生成、绑定与解绑。

涉及的 DTO：`LoginBody`（username/password/验证码/MFA/token、设备指纹等），`CaptchaVO`（UUID + Base64 图片），`LoginTokenVO`（accessToken/expiresIn/mfaRequired/mfaToken/needChangePassword），`MfaSetupVO`（secret + otpauth URI）。

## 编写 Feign 客户端
1. 新建 `com.sc.api.auth` 包，在其中声明 `@FeignClient(value = "sc-auth")` 接口（示例可参考 `sc-api-system` 中的 `RemoteUserService`）。
2. 使用 `com.sc.common.core.domain.R` 包装返回值，方便业务层统一判断。
3. 如果要访问 MFA 接口，请同时依赖 `RemoteUserService`（它负责同步用户的 MFA 状态）。

```java
@FeignClient(value = "sc-auth")
public interface RemoteAuthService {
    @PostMapping("/auth/login")
    R<LoginTokenVO> login(@RequestBody LoginBody body);
}
```

## 扩展建议
- 引入新的 Feign 客户端时，把 DTO 定在 `sc-auth` 模块已有的 `domain` 包，避免重复定义。
- 将新类加入 `sc-api-auth` 后，在 `sc-api/pom.xml` 的 `<modules>` 中注册，业务模块直接依赖即可复用统一行为。

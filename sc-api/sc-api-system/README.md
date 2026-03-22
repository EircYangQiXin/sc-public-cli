# sc-api-system 模块

## 定位
提供与 `sc-system` 用户管理服务的 Feign 合约，封装用户详情、MFA 配置、按角色查用户等内部接口，供其他服务（如 `sc-message` 的 MFA 交互）调用。

## 核心组件
- `RemoteUserService`：定义 `@FeignClient(value = "sc-system")`，所有方法统一通过 `com.sc.common.core.domain.R` 包装返回值，`fallbackFactory` 会在远程失败时返回 `R.fail` 并记录日志。
- `RemoteUserFallbackFactory`：记录调用错误并返回失败 `R`，确保业务侧可以捕获错误而非抛出异常。
- `SysUserDTO`：共享 DTO，包含 `userId`、`deptId`、`username`、`nickName`、`tenantId`、`roles`/`permissions`、`dataScope`、`dataScopeDeptIds`、`mfaEnabled`、`mfaSecret` 等字段。为避免泄露敏感信息，`password` 和 `mfaSecret` 在响应中会被清空。

## 接口清单
1. `@GetMapping("/system/user/info/{username}")`：根据用户名查用户详情。
2. `@PutMapping("/system/user/mfa")`：更新用户的 MFA 状态/密钥。
3. `@PostMapping("/system/user/internal/user-ids-by-roles")`：按角色 ID 列表返回用户 ID 列表（内部接口）。

所有接口都由 `FeignTokenInterceptor` 自动补充 `X-SC-Internal: sc-internal-feign`、Sa-Token 和 Same-Token，以满足后端 `SysUserController` 的安全校验。

## 使用示例
```
@Resource
private RemoteUserService remoteUserService;

R<SysUserDTO> info = remoteUserService.getUserInfo("admin");
if (info.isSuccess()) {
    SysUserDTO user = info.getData();
    // 处理用户信息
}
```

需要更新 MFA 时传入 `SysUserDTO` 的 `userId`、`mfaSecret`、`mfaEnabled`，`getUserIdsByRoleIds` 则返回目标角色下的全部用户 ID，空列表代表未命中。

## 注意事项
- 这些接口均为内部链路，Feign 拦截器已经添加了 `X-SC-Internal`，禁止直接暴露给外部客户端。
- 调用前务必检查 `R.isSuccess()`，`RemoteUserFallbackFactory` 会在失败场景中返回 `R.fail`。

# sc-api-message 模块

## 定位
封装了对 `sc-message` 站内信服务的 Feign 客户端，供其他模块（如 `sc-demo`、`sc-gateway`）发送内部通知或广播。

## 核心组件
- `RemoteMessageService`：`@FeignClient(value = "sc-message")`，定义 `sendInAppMessage` 方法调用 `/message/internal/send`。
- `RemoteMessageFallbackFactory`：服务降级时记录日志并返回 `R.fail`，避免业务端抛出异常。
- `InAppMessageDTO`：用于描述站内信对象，包括 `title`、`content`、`priority` (0=普通、1=重要、2=需要处理)、`receiverIds`。

## 方法契约
`sendInAppMessage(@RequestBody InAppMessageDTO dto)` 对应 `POST /message/internal/send`，该接口要求请求头包含 `X-SC-Internal: sc-internal-feign`（Feign 拦截器自动添加），同时会复用 `FeignTokenInterceptor` 附带 Sa-Token、Same-Token。

### DTO 字段
- `title`（必填）：通知标题。
- `content`（必填）：通知正文，可包含替换参数或 HTML。
- `priority`（可选）：优先级，0=普通，1=重要，2=需要用户确认。
- `receiverIds`（必填）：接收者用户 ID 列表，长度至少 1。广播需由业务自己构建全量 ID。

## 使用示例
```
InAppMessageDTO dto = InAppMessageDTO.builder()
        .title("系统维护")
        .content("22:00 进行发布，可能短暂无法登录")
        .priority(1)
        .receiverIds(Arrays.asList(102L, 233L))
        .build();
remoteMessageService.sendInAppMessage(dto);
```

## 注意事项
- `InAppMessageDTO` 不支持 `receiverIds` 为空；如果需要给全量用户发送，调用方需先构造完整用户 ID 列表或利用 `sc-message` 的广播 API。
- 所有调用均为内部链路，`RemoteMessageFallbackFactory` 会在目标服务不可用时向业务返回 `R.fail("...")`。

# sc-common-notify

## 模块定位

`sc-common-notify` 是统一通知中心抽象层，负责整合邮件、短信、站内信、Webhook 等多种通知渠道，并提供统一的请求模型与路由入口。

## 核心组件

- `NotificationChannel`：通知渠道统一接口
- `NotificationService`：通知发送入口与渠道路由
- `NotifyRequest`、`NotifyResult`：统一请求/响应模型
- `ChannelType`：渠道类型枚举
- `EmailChannel`、`SmsChannel`、`InAppChannel`、`WebhookChannel`：默认渠道实现
- `TemplateEngine`：模板渲染扩展点
- `NotifyAutoConfiguration`：默认 Bean 自动配置
- `@DefaultChannel`：标记模块内置默认渠道

## 渠道路由策略

当前实现不再依赖 Bean 注册顺序决定最终路由，而是遵循以下规则：

1. 按 `ChannelType` 分组
2. 同一渠道类型下，优先选择未标注 `@DefaultChannel` 的实现
3. 多个自定义实现同时存在时，再按 `@Order` 选择优先级更高的实现
4. 若出现多个候选实现，会打印警告日志，明确最终选中的渠道

这意味着业务方既可以继承默认实现，也可以直接实现 `NotificationChannel` 来替换默认短信或站内信逻辑。

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-notify</artifactId>
</dependency>
```

## 使用方式

### 发送通知

业务服务中注入 `NotificationService`，传入 `NotifyRequest` 即可按渠道类型路由发送。

### 自定义渠道

新增一个实现 `NotificationChannel` 的 Bean，并返回目标 `ChannelType`。如果要覆盖默认实现，不要标注 `@DefaultChannel`。

### 自定义模板渲染

实现 `TemplateEngine` 并注入 Spring 容器，即可扩展变量渲染逻辑。

## 适用场景

- 认证、告警、运营消息通知
- 站内信与外部渠道统一接入
- 多渠道能力的模块化扩展

## 注意事项

- 默认短信和站内信实现主要用于占位或示例，生产环境建议由业务方提供真实实现。
- 如果同一 `ChannelType` 存在多个自定义实现，应显式使用 `@Order` 管理优先级。

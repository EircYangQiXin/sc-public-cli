# sc-common-security

## 模块定位

`sc-common-security` 是整个项目的统一安全接入层，负责 Sa-Token 鉴权、登录上下文注入、Feign Token 透传和服务内部调用标识透传。

## 核心组件

- `SecurityConfig`：统一安全配置入口
- `SecurityInterceptor`：将登录态解析到业务上下文
- `StpInterfaceImpl`：权限与角色校验实现
- `FeignTokenInterceptor`：Feign 调用时自动透传 Token 与 `X-SC-Internal`
- `SecurityProperties`：安全配置项

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-security</artifactId>
</dependency>
```

## 配置说明

业务服务通常会配置：

```yaml
sc:
  security:
    ignore-urls:
      - /doc.html
      - /swagger-resources/**
      - /v3/api-docs/**
```

## 使用方式

### 控制器鉴权

接入服务后，可直接使用 Sa-Token 注解控制接口权限，例如登录校验、角色校验、权限校验。

### 读取登录用户

拦截器会将登录用户信息写入统一上下文，业务层可直接读取当前用户 ID、角色、权限等信息。

### Feign 调用

在启用 OpenFeign 的服务中，引入该模块后，`FeignTokenInterceptor` 会自动透传当前请求的认证信息；内部接口场景还会附带 `X-SC-Internal`。

## 适用场景

- 网关后的统一鉴权
- 微服务间身份透传
- 后台服务统一权限模型

## 注意事项

- `X-SC-Internal` 只是内部调用标识，不应等同于强鉴权凭证；如果服务端口暴露到外部，仍应结合网络边界或更强的服务间认证方案。
- Feign 透传依赖当前线程上下文，请避免在脱离请求上下文的异步线程里直接假定它一定存在。

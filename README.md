# SC Public CLI - 微服务脚手架

基于 **JDK 8 + Spring Boot 2.7 + Spring Cloud 2021** 的微服务脚手架项目。

## 技术栈

| 组件 | 技术 | 版本 |
|---|---|---|
| 权限认证 | Sa-Token | 1.37.0 |
| 注册/配置中心 | Nacos | 2021.0.5 |
| 网关 | Spring Cloud Gateway | 2021.0.8 |
| ORM | MyBatis-Plus | 3.5.5 |
| 缓存 | Redis + Redisson | 3.25.2 |
| 消息队列 | RabbitMQ | — |
| 对象存储 | AWS S3 协议（兼容 MinIO） | 1.12.626 |
| 接口文档 | Knife4j (OpenAPI 2) | 4.4.0 |
| 分布式事务 | Seata | 1.7.1 |
| 任务调度 | XXL-Job | 2.4.0 |
| 链路追踪 | Sleuth + Zipkin | — |
| 监控 | Actuator + Prometheus | — |

## 项目结构

```
sc-public-cli
├── sc-gateway          # 网关服务（鉴权、路由、限流）
├── sc-auth             # 认证服务（登录、验证码、令牌管理）
├── sc-modules
│   ├── sc-system       # 系统管理（用户/角色/菜单/字典/配置/日志/OSS）
│   └── sc-demo         # 示例服务
├── sc-api
│   └── sc-api-system   # System 模块 Feign API
├── sc-common
│   ├── sc-common-core      # 核心工具（R/异常/上下文/工具类）
│   ├── sc-common-redis     # Redis 缓存（CacheUtils/限流/防重复提交）
│   ├── sc-common-mybatis   # MyBatis-Plus（数据权限/租户/加密/批量插入）
│   ├── sc-common-security  # 安全模块（拦截器/权限/Feign 令牌透传）
│   ├── sc-common-swagger   # 接口文档（Knife4j 自动配置）
│   ├── sc-common-log       # 操作日志（注解+切面+事件）
│   ├── sc-common-oss       # 对象存储（S3 + STS 前端直传）
│   ├── sc-common-mq        # 消息队列（RabbitMQ 工具封装）
│   ├── sc-common-seata     # 分布式事务
│   ├── sc-common-xxljob    # 任务调度
│   └── sc-common-trace     # 链路追踪与监控（Sleuth/Zipkin/Actuator/Micrometer 统一管理）
├── sc-dependencies     # 依赖版本 BOM
├── sql                 # 数据库脚本
└── docker              # Docker 部署配置
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

### 构建

```bash
mvn clean install -DskipTests
```

### 启动顺序

1. Nacos、MySQL、Redis
2. `sc-gateway` (端口 8080)
3. `sc-auth` (端口 9200)
4. `sc-system` (端口 9201)

### 默认账号

- 用户名：`admin`
- 密码：`admin123`

### 接口文档

启动后访问网关聚合文档：`http://localhost:8080/doc.html`

## 内置功能

- ✅ 用户/角色/菜单/部门/字典/配置管理
- ✅ BCrypt 密码加密
- ✅ 操作日志 + 登录日志（异步持久化）
- ✅ 验证码（Kaptcha 图片 + Redis 存储）
- ✅ 敏感字段加密（@EncryptField + AES TypeHandler）
- ✅ 防重复提交（@RepeatSubmit + Redis SETNX）
- ✅ 文件上传（服务端代理 + STS 前端直传）
- ✅ 数据权限 + 多租户
- ✅ Redis 工具（KV/Hash/List/Set/限流/分布式锁）
- ✅ RabbitMQ 工具（4 种发送模式）
- ✅ 通用工具（ServletUtils/AssertUtils/JsonUtils/DateUtils）

## 需要注意配置项
```
sc:
  # 灰度路由配置
  gateway:
    gray:
      enabled: true
      # 签名密钥（用于内部网段校验）
      sign-secret: your-sign-secret-here
      # 允许访问的内部网段
      allowed-sources:
        - [IP_ADDRESS]
        - [IP_ADDRESS]
```
IP 可信代理: 若网关前有 Nginx/LB，请开启 sc.gateway.ip-access.trust-proxy=true 并配置 trusted-proxies
灰度安全: 至少配置 sc.gateway.gray.sign-secret 或 sc.gateway.gray.allowed-sources 之一

灰度签名调用方式变更
客户端现在需要按以下方式构造灰度请求：

```
timestamp = System.currentTimeMillis()
sign = Hex(HmacSHA256("serviceId:timestamp", signSecret))
Headers:
  X-Gray-Tag: gray
  X-Gray-Timestamp: {timestamp}
  X-Gray-Sign: {sign}
```

sc:
  security:
    lock:
      max-attempts: 5
      lock-minutes: 30
    password:
      min-length: 8
      expire-days: 90
    mfa:
      force-enabled: false
    device-trust:
      trust-days: 30

## 可观测性配置（必须）

> ⚠️ 所有服务的链路追踪和监控指标依赖以下 Nacos 共享配置，缺少会导致 Prometheus 抓取 404、Zipkin 无数据。

请在 **Nacos 共享配置**（如 `application-common.yml`）中添加：

```yaml
  spring:
    zipkin:
      base-url: ${ZIPKIN_BASE_URL:http://localhost:9411}
    sleuth:
      sampler:
        probability: ${SLEUTH_SAMPLER_PROBABILITY:0.1}

  management:
    endpoints:
      web:
        exposure:
          include: health,info,prometheus,metrics
    endpoint:
      health:
        show-details: when_authorized
    metrics:
      tags:
        application: ${spring.application.name}
      distribution:
        percentiles-histogram:
          http.server.requests: true
```

### 日志格式

所有服务已统一日志格式，日志中自动携带 `traceId` 和 `spanId`：

```
2026-03-20 14:30:00.123 [http-nio-9200-exec-1] [6a3f2b1c4d5e6f7a,8b9c0d1e2f3a4b5c] INFO  c.s.a.c.AuthController - 用户登录成功
```

接口响应头包含 `X-Trace-Id`，可通过该值在 Zipkin 中查询完整链路。

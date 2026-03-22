# sc-auth

## 模块定位

`sc-auth` 是统一认证服务，负责处理登录、登出、验证码、MFA 校验与 Token 刷新。
它通过 Feign 调用 `sc-system` 查询用户信息，并基于 Sa-Token 管理登录态。

## 核心能力

- 用户登录：校验用户名、密码、账号状态，并写入 Sa-Token Session。
- 图形验证码：通过 `CaptchaStrategy` 生成和校验验证码。
- 多因素认证：支持 TOTP 二次验证、设备信任、MFA 绑定与关闭。
- 账号安全策略：支持失败锁定、密码过期检查、设备信任有效期配置。
- 登录日志：登录成功、失败、MFA 校验等行为会发布日志事件。

## 关键目录

```text
src/main/java/com/sc/auth
├── controller    # 认证与 MFA 接口
├── service       # 登录流程编排
├── security      # 账号锁定、密码策略、TOTP、设备信任
├── captcha       # 验证码策略
└── domain        # 登录请求与返回对象
```

## 关键依赖

- `sc-common-security`：Sa-Token、登录上下文、安全拦截
- `sc-common-redis`：验证码、MFA 临时票据、账号锁定等 Redis 能力
- `sc-common-log`：登录日志事件
- `sc-common-swagger`：接口文档自动配置
- `sc-common-trace`：链路追踪与监控指标
- `sc-api-system`：远程查询用户信息、更新用户 MFA 状态

## 运行配置

服务名与端口定义在 [`bootstrap.yml`](/D:/WorkSpace/aiCode/sc-public-cli/sc-auth/src/main/resources/bootstrap.yml)：

- 服务名：`sc-auth`
- 端口：`9200`
- 配置中心：Nacos `application-common.yml`

核心配置项：

```yaml
sa-token:
  token-name: Authorization
  timeout: 86400
  active-timeout: 1800

sc:
  security:
    ignore-urls:
      - /auth/login
      - /auth/logout
      - /auth/captcha
```

安全策略配置由 [`SecurityProperties.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-auth/src/main/java/com/sc/auth/security/SecurityProperties.java) 读取：

```yaml
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
      token-expire-seconds: 300
      issuer: SC-CLI
    device-trust:
      trust-days: 30
```

## 使用方式

### 1. 作为独立服务启动

```bash
mvn -pl sc-auth spring-boot:run
```

或打包后运行：

```bash
mvn -pl sc-auth -am clean package -DskipTests
java -jar sc-auth/target/sc-auth-1.0.0.jar
```

### 2. 登录流程

接口由 [`AuthController.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-auth/src/main/java/com/sc/auth/controller/AuthController.java) 提供：

- `GET /auth/captcha`：获取图形验证码
- `POST /auth/login`：账号密码登录
- `POST /auth/mfa/verify`：二次 MFA 校验
- `POST /auth/logout`：退出登录
- `POST /auth/refresh`：刷新 Token

MFA 管理由 [`MfaController.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-auth/src/main/java/com/sc/auth/controller/MfaController.java) 提供：

- `POST /auth/mfa/setup`：生成密钥与二维码 URI
- `POST /auth/mfa/confirm`：确认启用 MFA
- `POST /auth/mfa/disable`：关闭 MFA

## 与其他模块的关系

- 依赖 `sc-system` 返回用户、角色、权限、MFA 信息。
- 与 `sc-gateway` 配合，由网关统一校验 Token 并转发请求。
- 登录日志事件最终由系统服务消费并落库。

## 注意事项

- 启动前必须先准备 Nacos、Redis，以及能访问 `sc-system` 的服务发现环境。
- `MfaController` 中的用户 MFA 启停依赖远程接口成功返回，发布时应确保 `sc-api-system` 契约与 `sc-system` 实现保持一致。
- 账号锁定、设备信任、MFA 临时票据都使用 Redis，开发和生产环境应区分库或 key 前缀。

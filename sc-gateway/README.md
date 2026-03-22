# sc-gateway

## 模块定位

`sc-gateway` 是统一 API 网关，负责服务路由、登录态校验、灰度路由、IP 访问控制、请求日志与聚合接口文档。

它是系统外部流量的统一入口，默认转发 `sc-auth`、`sc-system`、`sc-demo`、`sc-message` 四类服务。

## 核心能力

- 基于 Spring Cloud Gateway 的统一入口转发
- 基于 Sa-Token Reactor 的登录态校验
- 聚合 Knife4j 文档，统一暴露 `/doc.html`
- 灰度路由：支持 HMAC 签名或来源网段控制
- IP 黑白名单：支持直连 IP、可信代理、CIDR
- 内部调用头清洗：剥离外部伪造的 `X-SC-Internal`
- 请求日志与链路透传：将 `X-Trace-Id` 写入下游和响应头
- Sentinel Gateway 限流与熔断兜底

## 关键目录

```text
src/main/java/com/sc/gateway
├── config     # Sa-Token、CORS、灰度/IP 访问、Sentinel 配置
├── filter     # 全局过滤器
├── handler    # 网关异常与 Sentinel 兜底处理
└── ScGatewayApplication.java
```

## 路由与入口

路由定义位于 [`bootstrap.yml`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/resources/bootstrap.yml)：

- `/auth/**` -> `lb://sc-auth`
- `/system/**` -> `lb://sc-system`
- `/demo/**` -> `lb://sc-demo`
- `/message/**` -> `lb://sc-message`

基础信息：

- 服务名：`sc-gateway`
- 端口：`8858`
- 文档入口：`http://localhost:8858/doc.html`

## 关键过滤器

- [`SaTokenConfig.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/config/SaTokenConfig.java)
  负责统一鉴权与白名单排除。
- [`IpAccessFilter.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/filter/IpAccessFilter.java)
  负责黑白名单和可信代理场景下的来源 IP 识别。
- [`GrayRouteFilter.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/filter/GrayRouteFilter.java)
  负责灰度实例选择与请求重写。
- [`InternalHeaderStripFilter.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/filter/InternalHeaderStripFilter.java)
  负责剥离外部伪造的内部调用头。
- [`RequestLogFilter.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/filter/RequestLogFilter.java)
  负责请求日志、耗时统计和 `X-Trace-Id` 透传。

## 配置说明

### 灰度路由

配置实体见 [`GrayProperties.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/config/GrayProperties.java)：

```yaml
sc:
  gateway:
    gray:
      enabled: true
      sign-secret: your-sign-secret
      allowed-sources:
        - 10.0.0.0/8
```

当配置 `sign-secret` 时，客户端需构造以下请求头：

```text
X-Gray-Tag: gray
X-Gray-Timestamp: 当前毫秒时间戳
X-Gray-Sign: Hex(HmacSHA256("serviceId:timestamp", signSecret))
```

### IP 访问控制

配置实体见 [`IpAccessProperties.java`](/D:/WorkSpace/aiCode/sc-public-cli/sc-gateway/src/main/java/com/sc/gateway/config/IpAccessProperties.java)：

```yaml
sc:
  gateway:
    ip-access:
      enabled: true
      mode: blacklist
      trust-proxy: true
      trusted-proxies:
        - 10.0.0.0/8
      blacklist:
        - 192.168.1.100
      whitelist:
        - 192.168.1.0/24
```

## 使用方式

### 1. 本地启动

```bash
mvn -pl sc-gateway spring-boot:run
```

### 2. 打包运行

```bash
mvn -pl sc-gateway -am clean package -DskipTests
java -jar sc-gateway/target/sc-gateway-1.0.0.jar
```

### 3. 典型访问路径

- 登录：`POST /auth/login`
- 系统管理：`/system/**`
- 站内信：`/message/**`
- 示例服务：`/demo/**`

## 依赖关系

- 基础能力：`sc-common-core`
- 观测能力：`sc-common-trace`
- 服务治理：Nacos、Sentinel、Spring Cloud LoadBalancer
- 认证：Sa-Token Reactor + Redis

## 注意事项

- 外部请求默认经过网关；直连微服务端口时，网关过滤器不会生效。
- `InternalHeaderStripFilter` 只解决“经网关伪造内部头”的问题，服务间内部鉴权仍需要后端服务自行校验。
- 开启 `trust-proxy=true` 前，必须确认前置代理 IP 已配置到 `trusted-proxies`，否则可能错误信任转发头。

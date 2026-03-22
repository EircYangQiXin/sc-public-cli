# sc-common-trace

## 模块定位

`sc-common-trace` 统一接入链路追踪与观测能力，整合 Sleuth、Zipkin、Actuator、Prometheus，并补充响应头中的 `X-Trace-Id` 透出能力。

## 核心组件

- `TraceAutoConfiguration`：观测能力自动配置
- `TraceResponseFilter`：将当前链路的 `traceId` 写入响应头
- `logback-include.xml`：统一日志格式片段

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-trace</artifactId>
</dependency>
```

## 配置说明

推荐在 Nacos 共享配置中维护：

```yaml
spring:
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 0.1

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

## 使用方式

### 日志追踪

接入后，服务日志会自动携带 `traceId/spanId`，便于在 Zipkin 中查看完整链路。

### HTTP 响应排障

`TraceResponseFilter` 会把当前 `traceId` 写入响应头，客户端或网关可据此串联问题排查。

### 指标采集

Actuator 与 Prometheus Registry 已打通，Prometheus 可直接抓取 `/actuator/prometheus`。

## 注意事项

- 网关服务是 WebFlux，普通业务服务是 Servlet，本模块在两种场景下的使用方式略有差异，但目标一致。
- 采样率应结合实际流量调整，不建议在高流量生产环境中长期使用过高采样率。

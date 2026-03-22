# sc-common

## 模块定位
`sc-common` 是整套脚手架的共享组件集合，按功能拆成多个子模块（核心实体/工具、Redis、MyBatis-Plus、Sa-Token、Swagger、日志、MQ、OSS、Seata、XXL-Job、链路追踪、Excel、通知）。业务服务可以按需选择依赖子模块，也可以复用本级父 POM 管理版本。

## 子模块概览
- `sc-common-core`：统一返回值、分页/树结构、异常/上下文、常见工具类，所有业务模块的基础设施。
- `sc-common-redis`：增强的 `RedisTemplate`、Redisson lock、限流/缓存工具与 `@RepeatSubmit` 防重注入。
- `sc-common-mybatis`：定制的 MyBatis-Plus 拦截器（租户、数据权限、分页、乐观锁、全表更新保护）与自动填充。
- `sc-common-security`：Sa-Token 拦截器+上下文持有、Feign 请求自动携带 Token 与 `X-SC-Internal` header。
- `sc-common-swagger`：Knife4j/Swagger2 的自动 `Docket` 配置，按 `@Api` 过滤控制器。
- `sc-common-log`：`@OperationLog` 切面、事件驱动、操作/登录日志实体。
- `sc-common-mq`：RabbitMQ 基础配置、死信队列、Outbox 本地消息、幂等辅助与消息状态表。
- `sc-common-oss`：S3/MinIO 属性、`OssTemplate` 操作、STS 临时凭证封装。
- `sc-common-seata`：Seata Starter 的引导配置说明。
- `sc-common-xxljob`：XXL-Job 执行器自动装配。
- `sc-common-trace`：Zipkin/Brave `Tracer` 绑定的 HTTP 过滤器，确保 traceId/SpanId 透传。
- `sc-common-excel`：基于 EasyExcel 的导入导出封装工具。
- `sc-common-notify`：短信/邮件/站内信等通知渠道抽象与默认实现。

## 快速接入
业务模块在 `pom.xml` 中按需引入子模块：

```xml
<dependency>
  <groupId>com.sc</groupId>
  <artifactId>sc-common-core</artifactId>
</dependency>
<dependency>
  <groupId>com.sc</groupId>
  <artifactId>sc-common-mybatis</artifactId>
</dependency>
```

如果希望一起受 BOM 管理，可通过父 POM `sc-public-cli` 中的 `dependencyManagement` 引入模块。

## 常用配置
- `sc.security.ignore-urls`：Sa-Token 登录拦截排除路径。
- `sc.tenant.enabled` / `sc.tenant.column` / `sc.tenant.ignore-tables`：控制租户行级权限。
- `sc.oss.*`：（endpoint、accessKey、secretKey、bucketName、region、pathStyleAccess、roleArn、roleSessionName、stsDurationSeconds、stsEndpoint）用于 OSS/S3 + STS。
- `xxl.job.*`：XXL-Job admin、accessToken、执行器 appname/ip/port/log 等。

## 注意事项
- 每个子模块都以 `@Component`/`@Configuration` 形式自动扫描，业务模块需要在主应用的 `@SpringBootApplication` 同级包路径下才能被发现。
- 子模块间没有强依赖，按需引入、避免不必要的反射/配置负担。

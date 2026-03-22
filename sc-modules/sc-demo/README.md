# sc-demo

## 模块定位
`sc-demo` 是融合示例服务，主要用于展示与验证 `sc-system`、`sc-message` 及公共能力之间的集成链路，便于理解 Feign、RabbitMQ、XXL-JOB、OSS 的典型用法。

## 核心能力
- **Feign 调用**：`DemoController` 调用 `RemoteUserService` 获取用户信息，示例 `/demo/feign/{username}`。
- **RabbitMQ 生产者**：`/demo/mq/send` 接收消息并通过 `RabbitTemplate` 发送至 `demo.exchange`，适合作为 MQ 整合测试。
- **健康检测**：`/demo/health` 返回服务状态字符串，供监控使用。
- **XXL-JOB 任务**：`DemoJobHandler` 注册 `demoJobHandler`，在 XXL-Job 管理端触发执行，演示任务日志与 `@XxlJob` 注解。
- **OSS 资源**：通过 `sc-common-oss` 提供 MinIO/S3 上传示例，并在配置中指向 `sc-demo` 的 OSS 端点。

## 依赖与配置
- 引入 `sc-common-security`、`sc-common-mybatis`、`sc-common-swagger`、`sc-common-log`、`sc-common-mq`、`sc-common-xxljob`、`sc-common-oss`、`sc-common-seata`，以及 `sc-api-system`。
- 配置文件 `bootstrap.yml` 指定 `sc-demo` 名称、MySQL `sc_demo` 数据源、RabbitMQ 连接、XXL-Job Admin 地址、OSS（MinIO）信息。
- Nacos 配置与其他服务一致，使用 `application-common.yml` 共享；环境变量控制 `NACOS_ADDR`、`MYSQL_*`、`RABBITMQ_*`、`MINIO_*`、`XXLJOB_ADDR`。

## 启动与运行
1. 准备基础服务：Nacos、MySQL（`sc_demo`）、Redis、RabbitMQ、MinIO、XXL-Job Admin。
2. 运行方式：`mvn spring-boot:run -pl sc-modules/sc-demo` 或启动 `ScDemoApplication`。
3. 默认端口 `9202`，Swagger 文档可在 `http://localhost:9202/doc.html` 查看。
4. XXL-Job executor 端口默认 `9999`，可在 `bootstrap.yml` 中的 `xxl.job.executor` 部分调整。

## 示例路径
- `GET /demo/feign/{username}`：通过 `sc-api-system` 的 Feign 接口调用 `sc-system` 的 `/system/user/info`。
- `POST /demo/mq/send?message=xxx`：发送消息到 `demo.exchange`（配合 `sc-common-mq` 配置）。
- `GET /demo/health`：返回 `Demo Service is running!`。
- `XXL-Job`：在 XXL 管理端新增 `demoJobHandler` 任务，触发后在 `DemoJobHandler.demoJob` 打印日志。

## 注意
- 该模块主要为调研或集成验证使用，不作为生产业务服务。
- 与其他服务一样需要 Sa-Token 鉴权，默认 `sc-security` 忽略 `/demo/health` 与 Swagger 相关路径。

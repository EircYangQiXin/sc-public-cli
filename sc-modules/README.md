# sc-modules

## 概览
`sc-modules` 是业务服务聚合模块，负责封装后台治理、站内信与示例演示三个服务。每个子模块独立对外提供能力，聚合模块本身不对外打包。

## 子模块职责
- **sc-system**：核心管理服务，提供用户/角色/权限/菜单/字典/配置/日志/任务/OSS 等平台治理能力，并对外暴露 Feign 接口给其他服务。
- **sc-message**：站内信与公告中心，负责消息发送、查询、内部 API 推送以及广播与角色推送的 ACL 逻辑。
- **sc-demo**：示例服务，演示 Feign 调用、RabbitMQ 消息、XXL-JOB 调度、OSS 上传等一套完整的集成链路。

## 共享依赖与环境
- Spring Cloud + Alibaba（Nacos 注册与配置、Feign、Sentinel、LoadBalancer）驱动网关/服务发现。
- Sa-Token 安全、Redis 缓存、MyBatis-Plus + Flyway 数据迁移、Knife4j 文档、Trace/Actuator 等基础能力由 `sc-common` 模块提供。
- 每个子模块各自连接 MySQL（`sc_system`、`sc_message`、`sc_demo`），各自维护 Flyway 脚本。
- `sc-demo` 还依赖 RabbitMQ、MinIO（OSS）与 XXL-Job Admin。

## 配置约定
1. 所有服务的 `bootstrap.yml` 指定服务名、MySQL 连接、端口、Actuator 暴露与 Sa-Token 忽略路径。
2. 通用配置通过 Nacos `application-common.yml`（见 `config/application-common.yml`），环境变量覆盖常见地址：`NACOS_ADDR`、`MYSQL_*`、`RABBITMQ_*`、`MINIO_*`、`XXLJOB_ADDR` 等。
3. `sc-message` 内部接口必须携带 `X-SC-Internal: sc-internal-feign` 头用以防止非法调用。

## 编译与启动
1. 根目录运行 `mvn clean install -DskipTests`。
2. 单模块启动：通过 `mvn spring-boot:run -pl sc-modules/sc-system` 等方式，或直接运行相应 `Sc*Application`。
3. 建议先启动依赖组件：Nacos > MySQL/Redis/RabbitMQ/MinIO/XXL-Job > `sc-system` > `sc-message` > `sc-demo`。

## 发布建议
- 每个服务单独部署，维护独立日志与配置；重要接口可通过 `sc-gateway` 聚合统一入口。
- `sc-system` 提供的 Feign 接口（用户、角色等）应由其他服务通过 `sc-api-system` 调用。
- `sc-message` 的发信能力可通过管理员端或内部 Feign 客户端（`sc-api-message`）触发，`sc-demo` 可以作为运维工具演示 RabbitMQ/OSS/XXL 的整合。

## 参考
- 共享配置模板：`config/application-common.yml`
- 启动顺序建议：Nacos > MySQL/Redis/RabbitMQ/MinIO/XXL > sc-system > sc-message > sc-demo

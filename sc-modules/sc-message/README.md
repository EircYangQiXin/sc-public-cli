# sc-message

## 模块定位
`sc-message` 是站内信与公告服务，支持管理员端推送、角色/用户/全体发送、用户拉取、未读统计、详情展示、标记已读与内部 Feign 触发机制。

## 主要能力
- **用户视角**：`SysMessageController` 提供 `/message/list`、`/message/unread`、`/message/{id}`、`/message/read`、`/message/read-all` 等接口，均需登录且基于当前用户 ID 筛选。
- **管理视角**：`SysMessageAdminController` 提供权限打标的消息发布与分页列表操作，记录消息优先级、类型、接收范围。
- **内部调用**：`SysMessageInternalController` 暴露 `/message/internal/send`，要求 Feign 客户端发送 `X-SC-Internal: sc-internal-feign` 头，适合系统内的广播/角色/调度推送。
- **服务实现**：`SysMessageServiceImpl` 负责消息插入、receiver 记录、未读计数、ACL 校验与角色用户查询（通过 `RemoteUserService`）。

## 依赖与配置
- 依赖 `sc-common-security`、`sc-common-mybatis`、`sc-common-swagger`、`sc-common-log`、`sc-common-notify` 与 `sc-api-system`、`sc-api-message`。
- `bootstrap.yml` 为服务指定 `sc-message` 名称、MySQL `sc_message` 数据源、Flyway 结构同步、Actuator 暴露；`management.metrics.tags.application` 使用 `spring.application.name`。
- `spring.cloud.nacos` 配置与其他服务一致，共享 `application-common.yml`，通过 `NACOS_ADDR` 与 `MYSQL_*` 变量控制环境。

## 数据结构
- `sys_message`：消息主表，记录标题、内容、类型、发送范围、优先级与排他字段，包含多个索引（`idx_msg_type`、`idx_send_scope`、`idx_create_time`）。
- `sys_message_receiver`：接收者表，记录 message_id、receiver_id、是否已读、读取时间，带 `idx_receiver` 和 `idx_message` 索引，确保按用户/消息快速查询。
- Flyway 脚本位于 `src/main/resources/db/migration/V1.0.0__init_message.sql`，凡新环境需执行以建表。

## 接口与使用
- **管理员调用**：`/message/admin/send` 接受 `MessageSendDTO`，支持指定 `sendScope` (USER/ROLE/ALL) 与 `receiverIds`，内部自动构建 receiver 记录。
- **Feign 触发**：通过 `sc-api-message` 的 `RemoteMessageService` 或内部 `sys-message` Feign，触发 `/message/internal/send` 需传 `InAppMessageDTO` 并附加 `X-SC-Internal` 头。
- **消息查询**：客户端调用 `/message/list` 获取分页数据，后台 `SecurityContextHolder` 确保当前 userId；`/message/{id}` 附加 ACL 检查 `receiver` OR `send_scope='ALL'`。
- **未读控制**：`/message/unread`、`/message/read`、`/message/read-all` 提供未读统计与归档，`SysMessageServiceImpl` 维护 `sys_message_receiver` 状态。

## 运行说明
1. 依赖环境：Nacos、MySQL（`sc_message` 库）、Redis、RabbitMQ（若与通知集成）、`sc-system` 服务用于角色/用户查询。
2. 编译：`mvn spring-boot:run -pl sc-modules/sc-message`。
3. 默认端口 `9203`，Swagger 文档打开 `http://localhost:9203/doc.html`。

## 注意点
- `SysMessageInternalController` 仅允许内部调用，未携带正确 `X-SC-Internal` 值会被拒绝。
- 如果自己实现 `NotificationChannel` 以替换短信渠道，需要配合 `sc-common-notify` 新的冲突逻辑（@DefaultChannel 标记），确保消息通知不重复。

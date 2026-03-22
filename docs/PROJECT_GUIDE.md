# SC Public CLI 项目全景文档

## 1. 项目定位

`sc-public-cli` 是一个基于 `JDK 8 + Spring Boot 2.7 + Spring Cloud 2021 + Spring Cloud Alibaba 2021` 的微服务脚手架。

它当前的重点不是单一业务闭环，而是提供一套可复用的“后台治理 + 认证 + 通知 + 站内信 + 公共基础能力”组合模板，便于快速搭建中后台微服务系统。

## 2. 技术基线

- 认证鉴权：Sa-Token
- 注册与配置中心：Nacos
- 网关：Spring Cloud Gateway
- ORM：MyBatis-Plus
- 数据迁移：Flyway
- 缓存与分布式锁：Redis + Redisson
- 消息队列：RabbitMQ
- 对象存储：S3 协议 / MinIO
- 分布式事务：Seata
- 任务调度：XXL-JOB
- 链路与指标：Sleuth + Zipkin + Actuator + Prometheus + Grafana
- 接口文档：Knife4j

## 3. 架构总览

```text
                   ┌──────────────────────┐
                   │      sc-gateway      │
                   │ 路由 / 鉴权 / 灰度   │
                   └──────────┬───────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐   ┌────────▼────────┐   ┌────────▼────────┐
│    sc-auth     │   │    sc-system    │   │   sc-message    │
│ 登录 / MFA     │   │ 用户权限 / 配置 │   │ 站内信 / 公告   │
└───────┬────────┘   └────────┬────────┘   └────────┬────────┘
        │                     │                     │
        └──────────────┬──────┴──────────────┬──────┘
                       │                     │
                ┌──────▼──────┐      ┌──────▼──────┐
                │   sc-api    │      │  sc-common  │
                │ Feign 契约层 │      │ 公共基础能力 │
                └─────────────┘      └─────────────┘
```

## 4. 目录说明

### 根层模块

- [`sc-dependencies/README.md`](../sc-dependencies/README.md)：统一依赖版本 BOM
- [`sc-common/README.md`](../sc-common/README.md)：公共基础能力集合
- [`sc-api/README.md`](../sc-api/README.md)：Feign 契约层
- [`sc-auth/README.md`](../sc-auth/README.md)：统一认证服务
- [`sc-gateway/README.md`](../sc-gateway/README.md)：统一网关
- [`sc-modules/README.md`](../sc-modules/README.md)：业务服务集合

### 业务子模块

- [`sc-modules/sc-system/README.md`](../sc-modules/sc-system/README.md)：系统管理服务
- [`sc-modules/sc-message/README.md`](../sc-modules/sc-message/README.md)：站内信与公告服务
- [`sc-modules/sc-demo/README.md`](../sc-modules/sc-demo/README.md)：集成能力演示服务

### 公共子模块

- [`sc-common/sc-common-core/README.md`](../sc-common/sc-common-core/README.md)
- [`sc-common/sc-common-redis/README.md`](../sc-common/sc-common-redis/README.md)
- [`sc-common/sc-common-mybatis/README.md`](../sc-common/sc-common-mybatis/README.md)
- [`sc-common/sc-common-security/README.md`](../sc-common/sc-common-security/README.md)
- [`sc-common/sc-common-swagger/README.md`](../sc-common/sc-common-swagger/README.md)
- [`sc-common/sc-common-log/README.md`](../sc-common/sc-common-log/README.md)
- [`sc-common/sc-common-oss/README.md`](../sc-common/sc-common-oss/README.md)
- [`sc-common/sc-common-mq/README.md`](../sc-common/sc-common-mq/README.md)
- [`sc-common/sc-common-seata/README.md`](../sc-common/sc-common-seata/README.md)
- [`sc-common/sc-common-xxljob/README.md`](../sc-common/sc-common-xxljob/README.md)
- [`sc-common/sc-common-trace/README.md`](../sc-common/sc-common-trace/README.md)
- [`sc-common/sc-common-excel/README.md`](../sc-common/sc-common-excel/README.md)
- [`sc-common/sc-common-notify/README.md`](../sc-common/sc-common-notify/README.md)

### API 契约子模块

- [`sc-api/sc-api-system/README.md`](../sc-api/sc-api-system/README.md)
- [`sc-api/sc-api-auth/README.md`](../sc-api/sc-api-auth/README.md)
- [`sc-api/sc-api-message/README.md`](../sc-api/sc-api-message/README.md)

## 5. 服务边界

### sc-gateway

系统统一入口，负责：

- 路由转发
- Sa-Token 登录态校验
- IP 黑白名单
- 灰度路由
- 聚合 API 文档
- `X-Trace-Id` 透传

### sc-auth

负责认证域能力：

- 用户登录与登出
- 图形验证码
- MFA 二次认证
- 账号锁定策略
- 密码过期检查
- 设备信任

### sc-system

负责后台治理域能力：

- 用户、角色、菜单、部门、字典
- 系统配置
- 操作日志、登录日志
- 在线用户
- OSS 管理
- XXL-JOB 管理
- 对外 Feign 用户与角色查询能力

### sc-message

负责消息域能力：

- 站内信
- 公告
- 用户 / 角色 / 全员发送
- 未读统计
- 已读归档
- 供其他服务通过内部接口触发站内信

### sc-demo

负责能力演示：

- Feign 调用
- RabbitMQ 发送
- XXL-JOB 执行器
- OSS 配置示例

## 6. 配置体系

### 6.1 共享配置

所有服务都通过 `bootstrap.yml` 从 Nacos 加载共享配置：

- Data ID：`application-common.yml`
- Group：`DEFAULT_GROUP`

模板文件位于：

- [`config/application-common.yml.example`](/D:/WorkSpace/aiCode/sc-public-cli/config/application-common.yml.example)

### 6.2 环境变量

环境变量模板位于：

- [`.env.example`](/D:/WorkSpace/aiCode/sc-public-cli/.env.example)

主要包括：

- `NACOS_ADDR`
- `MYSQL_ADDR`、`MYSQL_USER`、`MYSQL_PWD`
- `REDIS_HOST`、`REDIS_PORT`、`REDIS_PWD`
- `RABBITMQ_*`
- `MINIO_*`
- `ZIPKIN_BASE_URL`
- `XXLJOB_ADDR`

### 6.3 各服务默认端口

- `sc-gateway`: `8858`
- `sc-auth`: `9200`
- `sc-system`: `9201`
- `sc-demo`: `9202`
- `sc-message`: `9203`

## 7. 数据与迁移

### 基础初始化

- [`sql/init.sql`](/D:/WorkSpace/aiCode/sc-public-cli/sql/init.sql)：系统管理相关初始化脚本
- [`sql/mq_local_message.sql`](/D:/WorkSpace/aiCode/sc-public-cli/sql/mq_local_message.sql)：本地消息表脚本

### Flyway

以下服务已经显式启用 Flyway：

- `sc-system`
- `sc-message`

它们分别在各自模块的 `src/main/resources/db/migration` 下维护迁移脚本。

## 8. 部署与开发环境

Docker 编排文件：

- [`docker/docker-compose.yml`](/D:/WorkSpace/aiCode/sc-public-cli/docker/docker-compose.yml)

当前编排覆盖：

- MySQL
- Redis
- Nacos
- Sentinel
- RabbitMQ
- MinIO
- Seata
- XXL-JOB Admin
- Zipkin
- Prometheus
- Grafana

## 9. 启动顺序建议

1. 启动基础设施：MySQL、Redis、Nacos
2. 按需启动 RabbitMQ、MinIO、Seata、XXL-JOB、Zipkin、Prometheus、Grafana
3. 启动 `sc-system`
4. 启动 `sc-auth`
5. 启动 `sc-message`
6. 启动 `sc-demo`
7. 启动 `sc-gateway`

说明：

- 如果 `sc-auth` 依赖 `sc-system` 查询用户信息，那么 `sc-system` 应优先启动。
- 如果只验证单服务接口，网关可放到最后启动。

## 10. 本地构建方式

全量构建：

```bash
mvn clean install -DskipTests
```

单模块运行示例：

```bash
mvn -pl sc-auth spring-boot:run
mvn -pl sc-gateway spring-boot:run
mvn -pl sc-modules/sc-system spring-boot:run
```

## 11. 当前项目特点

### 已具备的基础

- 模块边界清晰
- 公共能力拆分完整
- 微服务基础设施接入较全
- API 契约层已拆分
- 认证、后台治理、站内信三条主线已形成

### 更适合作为

- 微服务后台脚手架
- 二次开发模板
- 团队内部标准化基础工程

### 不应默认假设为

- 已完成全部生产级测试与安全封板
- 所有模块都经过冷启动与新库验证
- 开箱即用的完整业务产品

## 12. 当前已知风险与注意事项

- `sc-message` 的 Flyway 初始化脚本 [`V1.0.0__init_message.sql`](/D:/WorkSpace/aiCode/sc-public-cli/sc-modules/sc-message/src/main/resources/db/migration/V1.0.0__init_message.sql) 需要在新库场景下重点校验；当前代码库里该文件曾被发现存在 SQL 注释/字符串闭合问题，可能阻断冷启动迁移。
- `X-SC-Internal` 适合做“内部调用标识”，但不应单独作为对外暴露端口的强鉴权方案；如果服务端口对外可达，仍应依赖更严谨的网络边界或服务间认证机制。
- 现有文档与代码已基本覆盖主要模块，但自动化测试、CI、发布说明、许可证等公开发布基础设施仍建议继续补齐。

## 13. 推荐阅读顺序

如果你是首次接手这个项目，建议按以下顺序阅读：

1. 本文档
2. `sc-gateway` 与 `sc-auth`
3. `sc-system`
4. `sc-common` 相关子模块
5. `sc-message`
6. `sc-demo`
7. `sc-api`

## 14. 维护建议

- 新增服务时，优先复用 `sc-common-*` 与 `sc-api-*`，不要在业务服务内重复造基础设施代码。
- 需要跨服务调用时，优先先定义 `sc-api-*` 契约，再实现提供方和消费方。
- 涉及数据库变更时，优先采用 Flyway 管理，不再在 README 里维护手工 SQL 步骤。

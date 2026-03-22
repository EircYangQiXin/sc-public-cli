# sc-common-mq

## 模块定位

`sc-common-mq` 封装 RabbitMQ 常用能力，补齐死信、幂等消费、本地消息表和重试投递等通用基础设施。

## 核心组件

- `RabbitMqConfig`：RabbitMQ 基础配置
- `DeadLetterConfig`：死信交换机/队列相关配置
- `MqConstants`：消息常量
- `RabbitMqHelper`：消息发送辅助类
- `IdempotentHelper`：幂等消费辅助工具
- `LocalMessage`、`LocalMessageService`：本地消息表模型与服务
- `OutboxScheduledTask`：Outbox 补偿投递任务

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-mq</artifactId>
</dependency>
```

## 使用方式

### 普通消息发送

业务服务中注入 `RabbitMqHelper` 或直接使用 Spring AMQP 发送消息，公共模块负责统一基础配置。

### 幂等消费

在消费者侧结合 `IdempotentHelper` 与 Redis 标记处理状态，避免重复消费。

### 本地消息表

当业务需要可靠事件投递时，可使用 `LocalMessageService` 先将消息落入本地消息表，再由 `OutboxScheduledTask` 扫描并补偿投递。

## 相关脚本

- 本地消息表初始化脚本见 [`sql/mq_local_message.sql`](/D:/WorkSpace/aiCode/sc-public-cli/sql/mq_local_message.sql)

## 注意事项

- `IdempotentHelper` 依赖 Redis。
- `LocalMessageService` 与 Outbox 方案依赖 JDBC 数据源。
- 死信和重试策略是基础设施能力，具体交换机、队列命名仍建议由业务模块统一规划。

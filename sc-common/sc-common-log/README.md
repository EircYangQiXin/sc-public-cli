# sc-common-log

## 模块定位

`sc-common-log` 提供操作日志与登录日志的统一采集能力，采用“注解 + AOP + 事件”的方式将日志采集与日志落库解耦。

## 核心组件

- `@OperationLog`：标记需要采集操作日志的方法
- `OperationLogAspect`：切面拦截方法执行，生成操作日志事件
- `OperationLogEvent`：操作日志事件
- `LoginLogEvent`：登录日志事件
- `BusinessType`、`OperatorType`：日志分类枚举

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-log</artifactId>
</dependency>
```

## 使用方式

### 采集操作日志

在 Controller 或 Service 方法上添加 `@OperationLog`，并设置业务类型、标题、操作人类型等元数据。

### 发布登录日志

认证模块或业务模块可直接发布 `LoginLogEvent`，由监听方统一落库。例如 `sc-auth` 的登录流程就是这样接入的。

### 在业务服务中消费

日志事件的持久化通常由业务服务自行监听并落表，例如 `sc-system` 中的日志监听器负责最终存储。

## 适用场景

- 后台管理操作审计
- 登录成功/失败记录
- 将日志采集逻辑从业务逻辑中剥离

## 注意事项

- 本模块只负责采集和发布，不负责最终入库表结构；落库由接入方决定。
- 如果方法参数中包含敏感信息，建议在业务侧避免直接原样记录。

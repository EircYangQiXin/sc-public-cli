# sc-common-seata

## 模块定位

`sc-common-seata` 用于统一接入 Seata 分布式事务能力，为需要跨服务事务协调的业务模块提供自动配置基础。

## 核心组件

- `SeataAutoConfiguration`：Seata 自动装配入口

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-seata</artifactId>
</dependency>
```

## 使用方式

### 接入全局事务

业务服务引入该模块后，补齐 `seata.*` 配置，并在业务方法中结合 Seata 注解或事务编排实现全局事务。

### 运行前准备

- 准备 Seata Server
- 准备事务日志表和业务数据库
- 确认注册中心/配置中心与当前环境一致

## 适用场景

- 订单、库存、账户等跨服务事务
- Saga/AT 模式统一接入

## 注意事项

- 本模块只提供接入层，是否实际启用分布式事务仍由业务服务配置决定。
- 分布式事务会引入额外复杂度，不建议为了简单场景默认开启。

# sc-common-mybatis

## 模块定位

`sc-common-mybatis` 是整个项目的数据访问基础模块，负责 MyBatis-Plus、Druid、Flyway 及数据权限、字段加密、租户预留等能力的统一接入。

## 核心组件

- `MybatisPlusConfig`：MyBatis-Plus 总配置
- `AutoFillMetaObjectHandler`：创建人、更新时间等字段自动填充
- `DataPermissionInterceptor`：数据权限拦截器
- `@DataScope`：数据权限声明注解
- `BatchInsertInjector`：批量插入扩展
- `EncryptField`、`EncryptTypeHandler`、`AesEncryptStrategy`：敏感字段加密能力
- `ScTenantLineHandler`、`TenantProperties`：多租户预留能力

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-mybatis</artifactId>
</dependency>
```

## 使用方式

### 基础 ORM

业务服务引入依赖后，即可直接使用 MyBatis-Plus 的 Mapper、Service 和分页能力。

### 数据权限

在查询方法上使用 `@DataScope`，由拦截器结合当前登录上下文拼装权限范围。

### 字段加密

对敏感字段添加 `@EncryptField` 并使用 `EncryptTypeHandler`，即可在持久化层进行透明加解密。

### Flyway

业务服务可直接通过 `spring.flyway.*` 配置使用 Flyway，数据迁移脚本由各服务自行维护。

## 适用场景

- 后台服务通用数据库访问
- 审计字段自动填充
- 数据权限隔离
- 敏感字段加密
- 多租户扩展

## 注意事项

- 租户能力当前属于“可开关的预留能力”，实际是否启用取决于业务服务中的 `sc.tenant.enabled` 配置。
- 数据权限与租户规则都依赖当前登录上下文，未登录或内部任务场景需要额外确认行为是否符合预期。

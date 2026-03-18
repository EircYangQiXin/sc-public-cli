# SC Public CLI 项目规则

## 一、项目概述

本项目是基于 **JDK 8 + Spring Boot 2.7.x + Spring Cloud 2021.0.x** 的微服务脚手架，使用 Sa-Token 做权限认证，Nacos 做注册/配置中心。

## 二、技术规范

### 2.1 JDK 版本
- 必须兼容 **JDK 8**，禁止使用 JDK 9+ 的 API（如 `List.of()`、`var`、`HttpClient` 等）

### 2.2 接口文档（Knife4j / Swagger）
- 所有对外 Controller 方法**必须**添加 `@ApiOperation` 注解说明接口用途
- 所有请求参数（DTO / Query / PathVariable / RequestParam）**必须**添加文档注解：
  - 实体字段使用 `@ApiModelProperty(value = "字段说明", required = true/false, example = "示例值")`
  - `@RequestParam` 使用 `@ApiParam(value = "说明")`
  - `@PathVariable` 使用 `@ApiParam(value = "说明")`
- Controller 类必须添加 `@Api(tags = "模块名称")` 注解
- 请求体（`@RequestBody`）对应的 DTO/Entity 类必须添加 `@ApiModel(description = "说明")` 注解

### 2.3 设计模式
- 创建功能需求时，**优先考虑使用设计模式**来降低代码复杂度和提高扩展性：
  - **策略模式**：适用于多种算法/行为可互换的场景（如不同类型的消息推送、不同渠道的支付、不同平台的第三方登录）
  - **模板方法模式**：适用于流程固定但步骤可变的场景（如审批流程、数据导入导出）
  - **工厂模式**：适用于需要根据条件创建不同对象的场景
  - **观察者模式 / Spring Event**：适用于解耦事件触发与处理的场景（如操作日志、消息通知）
  - **建造者模式**：适用于复杂对象的分步构建
  - **装饰器模式**：适用于动态增强功能的场景
- 不要为了使用设计模式而过度设计，简单场景保持简单实现

## 三、编码规范

### 3.1 项目结构
- `sc-common-*` 通用模块通过 `spring.factories` 实现自动配置
- 包结构遵循：`com.sc.{模块名}.{层级}`（controller / service / mapper / domain）
- domain 下分：`entity`（数据库实体）、`vo`（视图对象）、`query`（查询参数）、`dto`（传输对象）

### 3.2 返回值
- 所有接口统一使用 `R<T>` 返回
- 分页接口返回 `R<PageResult<T>>`

### 3.3 异常处理
- 业务异常使用 `ServiceException`，由 `GlobalExceptionHandler` 统一处理
- 禁止在 Controller 中 try-catch 后吞掉异常

### 3.4 数据库
- 实体类继承 `BaseEntity`，自动填充 createBy/updateBy/createTime/updateTime/delFlag
- 使用 MyBatis-Plus 的 `LambdaQueryWrapper` 构建查询条件
- 数据库变更必须通过 Flyway 迁移脚本管理（`src/main/resources/db/migration/`）

### 3.5 权限
- 需要鉴权的接口使用 `@SaCheckPermission("模块:功能:操作")` 注解
- 需要登录的接口使用 `@SaCheckLogin` 注解
- 内部 Feign 调用的接口不加权限注解，但需配置在 `SecurityProperties.ignoreUrls` 中

### 3.6 日志
- 增删改操作使用 `@OperationLog` 注解记录操作日志
- 查询接口不需要操作日志

## 四、配置规范

- 禁止在代码中硬编码配置值，统一通过 `bootstrap.yml` 或 Nacos 配置
- 敏感配置使用环境变量占位符：`${ENV_VAR:defaultValue}`
- 多环境通过 Nacos namespace 隔离（dev / test / prod）

## 五、禁止事项

- ❌ 禁止读取项目内的配置文件（yaml/yml/properties/conf），如需这些文件请主动询问用户
- ❌ 禁止使用 JDK 9+ 语法和 API
- ❌ 禁止在 Controller 中编写业务逻辑，业务逻辑必须放在 Service 层
- ❌ 禁止直接在 SQL 中拼接参数

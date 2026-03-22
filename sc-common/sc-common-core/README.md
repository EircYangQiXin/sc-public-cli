# sc-common-core

## 模块定位
提供最底层的公共能力：统一的响应对象、分页/树数据结构、上下文/异常持有器和常用工具。其他模块（如业务服务、通知、日志、安全）都依赖其基础设施。

## 目录与核心组件
- `com.sc.common.core.domain`：`BaseEntity`（ID/审计字段）、`PageQuery`/`PageResult` 分页封装、`R` 统一响应。
- `com.sc.common.core.context.SecurityContextHolder`：线程上下文，`sc-common-security` 会在拦截器中写入当前登录用户信息。
- `com.sc.common.core.exception`：`ServiceException` 作为业务异常、`GlobalExceptionHandler` 统一映射 HTTP 返回。
- `com.sc.common.core.utils`：`AssertUtils`、`DateUtils`、`JsonUtils`、`ServletUtils`、`TreeUtils`、`PasswordUtils` 等常见工具方法。
- `com.sc.common.core.constant.Constants`：常量定义（如 `LOGIN_USER_KEY`等）供各层复用。

## 依赖方式
```xml
<dependency>
  <groupId>com.sc</groupId>
  <artifactId>sc-common-core</artifactId>
</dependency>
```

## 使用说明
- 控制器可直接返回 `R.ok(payload)`/`R.fail(message)`，`GlobalExceptionHandler` 会捕获 `ServiceException` 并保持一致格式。
- 继承 `BaseEntity` 的实体自动拥有 `id/createTime/updateTime/createBy/updateBy/delFlag`，结合 `sc-common-mybatis` 的自动填充最小化样板。
- 通过 `SecurityContextHolder.getUserId()` 获取当前登录用户 ID，配合 `sc-common-security` 中的拦截器。

## 注意事项
- 该模块没有自身配置项，只靠类加载即可工作。确保 Spring 扫描路径包含 `com.sc.common.core`。
- `GlobalExceptionHandler` 直接将 `ServiceException` 的错误码/消息暴露给前端，敏感信息请控制在 `ServiceException` 构造时过滤。

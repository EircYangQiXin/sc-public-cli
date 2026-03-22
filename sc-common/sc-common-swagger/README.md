# sc-common-swagger

## 模块定位

`sc-common-swagger` 统一封装 Knife4j + Swagger2 配置，减少各服务重复编写文档配置代码，并补充接口版本控制扩展。

## 核心组件

- `SwaggerConfig`：Knife4j/Swagger 基础配置
- `@ApiVersion`：接口版本标记注解
- `ApiVersionAutoConfiguration`：版本路由自动配置
- `ApiVersionRequestMappingHandlerMapping`：请求映射扩展

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-swagger</artifactId>
</dependency>
```

## 使用方式

### 开启文档

在 Web 服务中引入本模块后，通常无需重复声明 Swagger 配置，只需正常使用 `@Api`、`@ApiOperation` 等注解即可。

### 使用版本注解

如果某类接口需要版本区分，可在控制器或方法上使用 `@ApiVersion`，由自定义 HandlerMapping 参与路径匹配。

## 访问方式

- 单服务：`http://{host}:{port}/doc.html`
- 网关聚合：`http://{gateway-host}:{gateway-port}/doc.html`

## 注意事项

- 本模块基于 Swagger2/Knife4j 4.x。
- 是否在生产环境暴露文档，应由网关与服务侧安全策略共同控制。

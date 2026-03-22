# sc-common-xxljob

## 模块定位

`sc-common-xxljob` 用于统一接入 XXL-JOB 执行器配置，减少各业务服务重复编写接入代码。

## 核心组件

- `XxlJobConfig`：XXL-JOB 执行器自动配置

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-xxljob</artifactId>
</dependency>
```

## 配置说明

常见配置如下：

```yaml
xxl:
  job:
    admin:
      addresses: http://localhost:8081/xxl-job-admin
    executor:
      appname: sc-demo
      port: 9999
```

## 使用方式

### 注册任务处理器

业务服务中声明带 `@XxlJob` 注解的方法即可注册任务处理器，例如 `sc-demo` 中的 `DemoJobHandler`。

### 启动执行器

服务启动后会自动向 XXL-JOB Admin 注册当前执行器。

## 适用场景

- 定时同步
- 补偿任务
- 后台批处理

## 注意事项

- 需要先部署 XXL-JOB Admin。
- 执行器端口、日志目录和访问令牌应根据环境单独配置。

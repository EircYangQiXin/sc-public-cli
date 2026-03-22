# sc-dependencies

## 模块定位

`sc-dependencies` 是整个工程的依赖版本 BOM，用于统一管理 Spring、Spring Cloud、中间件、工具库以及内部模块版本。

它本身不产出业务代码，也不会单独运行；作用是降低多模块版本漂移风险，保证各服务的依赖组合一致。

## 管理范围

### 基础框架

- Spring Boot `2.7.18`
- Spring Cloud `2021.0.8`
- Spring Cloud Alibaba `2021.0.5.0`

### 数据与 ORM

- MyBatis-Plus `3.5.5`
- Druid `1.2.21`
- Flyway `8.5.13`
- MySQL 驱动 `8.0.33`

### 安全与认证

- Sa-Token `1.37.0`

### 中间件

- Redisson `3.25.2`
- Seata `1.7.1`
- XXL-JOB `2.4.0`
- AWS S3 SDK `1.12.626`

### 工具链

- Knife4j `4.4.0`
- Hutool `5.8.25`
- Lombok `1.18.30`
- MapStruct `1.5.5.Final`
- EasyExcel `3.3.4`

### 内部模块

`sc-common-*`、`sc-api-*` 的版本均由 `sc.version` 统一控制，当前为 `1.0.0`。

## 使用方式

父工程已经在根 [`pom.xml`](/D:/WorkSpace/aiCode/sc-public-cli/pom.xml) 中导入该 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.sc</groupId>
            <artifactId>sc-dependencies</artifactId>
            <version>${project.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

子模块通常只需要声明依赖，不必重复写版本号：

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-security</artifactId>
</dependency>
```

## 适用场景

- 新增业务服务时复用现有依赖版本体系
- 新增公共模块时纳入统一版本管理
- 升级 Spring 或中间件版本时集中处理兼容性

## 维护建议

- 优先在本模块调整版本，再联动验证所有服务编译与启动。
- 大版本升级时，建议按“BOM 调整 -> 公共模块验证 -> 业务服务验证”的顺序执行。
- 内部模块一旦拆分独立发布，`sc.version` 的发布节奏也需要与 Maven 仓库版本策略同步。

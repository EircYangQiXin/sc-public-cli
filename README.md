# SC Public CLI — Spring Cloud 微服务脚手架

## 技术栈

| 分层 | 技术 |
|---|---|
| 基础框架 | Spring Boot 2.7.x + Spring Cloud 2021.0.x + JDK 8 |
| 注册/配置 | Nacos 2.2.x（多环境 namespace 隔离） |
| 网关 | Spring Cloud Gateway |
| 权限认证 | Sa-Token |
| 数据库 | MySQL 8 + MyBatis-Plus + Flyway |
| 缓存 | Redis + Redisson |
| 接口文档 | Knife4j (OpenAPI) |
| 熔断限流 | Sentinel |
| 对象存储 | MinIO / S3 兼容 |
| 定时任务 | XXL-JOB |
| 消息队列 | RabbitMQ |
| 分布式事务 | Seata AT 模式 |
| 链路追踪 | Sleuth + Zipkin |
| 监控 | Actuator + Prometheus + Grafana |

## 模块结构

```
sc-public-cli/
├── sc-dependencies       # BOM 版本管理
├── sc-common             # 通用模块集合
│   ├── sc-common-core    # 统一响应/全局异常/工具类
│   ├── sc-common-redis   # Redis + Redisson 封装
│   ├── sc-common-mybatis # MyBatis-Plus + 数据权限 + 多租户(预留)
│   ├── sc-common-security# Sa-Token 权限封装
│   ├── sc-common-swagger # Knife4j 文档配置
│   ├── sc-common-log     # 操作日志注解 + AOP
│   ├── sc-common-oss     # S3 对象存储封装
│   ├── sc-common-mq      # RabbitMQ 配置
│   ├── sc-common-seata   # Seata 分布式事务
│   └── sc-common-xxljob  # XXL-JOB 执行器配置
├── sc-api                # Feign 远程调用接口层
├── sc-auth               # 认证服务(登录/Token)
├── sc-gateway            # 统一网关
├── sc-modules
│   ├── sc-system         # 系统管理(用户/角色/菜单/部门/字典)
│   └── sc-demo           # 示例业务服务
├── sql/                  # 数据库初始化脚本
├── docker/               # Docker Compose 环境
└── docs/                 # 项目文档
```

## 快速开始

### 1. 启动中间件

```bash
cd docker
docker compose up -d
```

### 2. 初始化数据库

```bash
# 方式1: Docker 已自动执行 sql/init.sql
# 方式2: 手动执行
mysql -uroot -proot < sql/init.sql
```

### 3. 配置 Nacos

1. 访问 `http://localhost:8848/nacos`（默认账号: nacos/nacos）
2. 创建命名空间: `dev`, `test`, `prod`
3. 在 `dev` 空间下创建共享配置 `application-common.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 1.0

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### 4. 启动服务（按顺序）

```bash
# 1. 编译
mvn clean install -DskipTests

# 2. 启动顺序
ScGatewayApplication      # 网关 :8080
ScAuthApplication         # 认证 :9200
ScSystemApplication       # 系统 :9201
ScDemoApplication         # 示例 :9202
```

### 5. 访问

| 服务 | 地址 |
|---|---|
| 网关 | http://localhost:8080 |
| 聚合文档 | http://localhost:8080/doc.html |
| Nacos | http://localhost:8848/nacos |
| Sentinel | http://localhost:8858 |
| RabbitMQ | http://localhost:15672 |
| MinIO | http://localhost:9001 |
| XXL-JOB | http://localhost:8081/xxl-job-admin |
| Zipkin | http://localhost:9411 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

## 如何新增业务模块

1. 在 `sc-modules/` 下新建子模块，参照 `sc-demo` 的 POM 和配置
2. 如需对外暴露 Feign 接口，在 `sc-api/` 下新建对应的 API 模块
3. 在 `sc-gateway` 的 `bootstrap.yml` 中添加路由规则
4. 在根 `pom.xml` 的 `<modules>` 中注册新模块

## 环境隔离

通过启动参数切换环境：

```bash
# 开发环境
-DNACOS_NAMESPACE=dev

# 测试环境
-DNACOS_NAMESPACE=test

# 生产环境
-DNACOS_NAMESPACE=prod
```

## 数据权限

在 Mapper 方法上添加 `@DataScope` 注解即可启用数据权限过滤：

```java
@DataScope(deptAlias = "d", userAlias = "u")
List<SysUser> selectUserList(SysUser user);
```

## 多租户（预留）

在配置中启用：

```yaml
sc:
  tenant:
    enabled: true
    column: tenant_id
    ignore-tables:
      - sys_menu
      - sys_dict_type
```

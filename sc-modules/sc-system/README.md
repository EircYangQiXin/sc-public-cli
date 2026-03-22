# sc-system

## 模块定位
`sc-system` 是平台级后台治理服务，承担用户/权限/组织/菜单/字典/配置/日志/任务等管理能力，并对外提供 Feign 接口供其他服务调用。

## 核心能力
- **用户与认证**：`SysUserController`、`SysUserSocialController` 支持 Sa-Token 登录、社交登录绑定、用户信息及权限查询。
- **权限体系**：`SysRoleController`、`SysMenuController` 维护角色、菜单、权限树，`OnlineUserController` 提供会话管理。
- **运营配置**：`SysConfigController` 管理动态配置；`SysDictController`、`SysDeptController` 提供字典与组织结构管理。
- **审计日志**：`SysOperLogController`、`SysLoginLogController`、`SysJobLogController` 赋能操作/登录/任务日志查询。
- **文件与任务**：`OssController` 对接 OSS（MinIO/S3），`SysJobController` 结合 XXL-Job 任务调度。
- **公共资源**：`sc-common-log` 提供统一操作日志注解，`sc-common-security` 与 `sc-common-trace` 统一安全与链路上下文。

## 依赖与环境
- 引入 `sc-common-security`、`sc-common-mybatis`、`sc-common-swagger`、`sc-common-log` 以及 Feign 接口 `sc-api-system`。
- 使用 Spring Boot Web、Nacos 注册/配置、Feign、LoadBalancer、Sentinel，桥接 `sc-common-trace` 链路。
- 通过 `bootstrap.yml` 配置 `sc_system` 数据库、Nacos 地址、Actuator 暴露（health/info/prometheus）与 Sa-Token 忽略路径。

## 数据迁移
- Flyway 在 `src/main/resources/db/migration` 中维护多个版本脚本（`V1.0.0__init.sql`、`V1.1.0__add_social_and_menus.sql` 等），确保启动时完成表结构与权限基础数据初始化。
- 建议新环境顺序执行 Flyway，可通过 `spring.flyway.baseline-on-migrate` 保证数据共存。

## 启动说明
1. 确保 Nacos、MySQL（`sc_system` 库）、Redis、MinIO（若启用）可用。
2. 设置环境变量：`NACOS_ADDR`、`MYSQL_*`、`RABBITMQ_*`（若需要）等。
3. 运行 `mvn spring-boot:run -pl sc-modules/sc-system` 或执行 `ScSystemApplication`。
4. 默认端口 `9201`，启动后可访问 `http://localhost:9201/doc.html` 查看 Knife4j 文档。

## 常用接口
- `POST /system/user/login`：Sa-Token 登录；
- `GET /system/user/info`：获取当前用户权限/菜单；
- `GET /system/menu/list`、`/system/role/list` 等：菜单/角色管理；
- `POST /system/config/save`、`/system/dict/save`：配置字典维护；
- `POST /system/job/add` + `/api/system/user/internal/user-ids-by-roles`：任务与角色查询 Feign 接口；
- `POST /system/oss/upload`：OSS 文件上传。

## 注意
- 所有管理接口默认开启 Sa-Token 权限，需配合 `sc-common-security` 的 `@SaCheckPermission` 注解使用。
- 若使用 `sc-gateway`，建议通过网关统一暴露 `/system/**` 接口并开启 API 文档。

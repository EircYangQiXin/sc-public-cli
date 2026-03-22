# sc-api 模块

## 定位
`sc-api` 是所有 Feign 合约的聚合模块，封装了对 `sc-system`/`sc-auth`/`sc-message` 等服务的远程调用客户端，便于业务服务统一依赖接口契约。

## 模块组成
- `sc-api-system`：对 `sc-system` 的用户信息、角色、MFA 等内部接口进行调用。
- `sc-api-auth`：面向 `sc-auth` 的认证和 MFA 接口，目前作为 Feign 契约占位，未来可在此补全客户端。
- `sc-api-message`：对 `sc-message` 的站内信发送 API。

## 依赖与接入
- 所有子模块都继承了 `sc-common-core`（统一 `R`、`LoginUser` 等基础类型）和 `spring-cloud-starter-openfeign`。
- 业务模块只需引入 `sc-api`（或相应子模块）并保证主应用启用了 Feign 扫描：`@EnableFeignClients(basePackages = "com.sc.api")`。
- `sc-common-security` 的 `FeignTokenInterceptor` 会自动附加 Sa-Token、Same-Token 和 `X-SC-Internal: sc-internal-feign`，确保内部接口的鉴权/路由一致。

## 调用注意点
- 所有 Feign 客户端都返回 `com.sc.common.core.domain.R<T>`，调用方应先检查 `isSuccess()`，再读取 `getData()`。
- 其中多个接口只有内部服务可访问，依赖 `X-SC-Internal` 头，不能直接对外暴露。

## 扩展建议
- 若需接入新的服务，新增 `sc-api-xxx` 子模块、定义 `@FeignClient`/DTO/fallback，并把它注册到 `sc-api/pom.xml` 的 `<modules>`。
- 业务服务在需要调用时只需在 `pom` 中依赖新增的模块即可继续复用 Feign 公共行为。

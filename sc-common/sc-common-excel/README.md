# sc-common-excel

## 模块定位

`sc-common-excel` 提供基于 EasyExcel 的导入导出基础能力，适合在后台管理服务中统一处理 Excel/CSV 数据交换。

## 核心组件

- `ExcelAutoConfiguration`：自动装配 Excel 相关能力
- `ExcelHelper`：统一封装导入导出流程
- `AbstractImportListener`：导入监听器基类，负责收集校验与错误信息
- `ImportResult`：导入结果对象
- `PageDataSupplier`：适合分页导出场景的数据供给接口

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-excel</artifactId>
</dependency>
```

## 使用方式

### 导出

在 Web 模块中引入本依赖后，可以基于 `ExcelHelper` 统一输出文件流，避免各服务重复拼装 EasyExcel 配置。

### 导入

定义导入 DTO 后，继承 `AbstractImportListener` 处理每行数据的校验、转换和批量落库逻辑，最终返回 `ImportResult` 汇总成功数、失败数和错误信息。

### 分页大数据量导出

当导出数据量较大时，可实现 `PageDataSupplier`，按页提供数据，降低一次性加载全部数据的内存压力。

## 适用场景

- 后台管理数据批量导入
- 字典、组织、用户等基础资料模板导出
- 大批量分页导出

## 注意事项

- 本模块依赖 `spring-webmvc` 和 `javax.servlet-api` 的 `provided` 能力，通常应在 Web 服务中使用。
- 导入模板与 DTO 字段映射建议由业务模块自行维护，公共模块只负责通用流程。

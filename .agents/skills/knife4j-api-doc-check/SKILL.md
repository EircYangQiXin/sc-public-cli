---
name: Knife4j 接口文档注解验证
description: 在创建或修改 Controller 接口时，自动检查并确保所有必要的 Knife4j/Swagger 文档注解已正确添加，保证接口能在接口文档中被正常查看。
---

# Knife4j 接口文档注解验证

## 触发时机

当以下操作发生时，**必须**执行本 Skill 的检查流程：

1. 新建 Controller 类
2. 在现有 Controller 中新增接口方法
3. 修改现有接口的参数或返回值
4. 新建或修改 DTO / VO / Query / Entity 等请求/响应对象

---

## 检查清单

### 1. Controller 类级别

| 检查项 | 必须 | 注解 | 示例 |
|--------|:----:|------|------|
| 类上添加模块标签 | ✅ | `@Api(tags = "模块名称")` | `@Api(tags = "用户管理")` |

### 2. 接口方法级别

| 检查项 | 必须 | 注解 | 示例 |
|--------|:----:|------|------|
| 方法用途说明 | ✅ | `@ApiOperation("接口说明")` | `@ApiOperation("分页查询用户列表")` |

### 3. 请求参数级别

| 参数类型 | 必须 | 注解 | 示例 |
|----------|:----:|------|------|
| `@PathVariable` | ✅ | `@ApiParam(value = "说明", required = true)` | `@ApiParam(value = "用户ID", required = true) @PathVariable Long userId` |
| `@RequestParam` | ✅ | `@ApiParam(value = "说明")` | `@ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum` |
| `@RequestBody` | ✅ | 对应的类需要添加 `@ApiModel` 和 `@ApiModelProperty`（见下方） | — |

### 4. 请求/响应对象级别（DTO / VO / Query / Entity）

| 检查项 | 必须 | 注解 | 示例 |
|--------|:----:|------|------|
| 类上添加模型描述 | ✅ | `@ApiModel(description = "说明")` | `@ApiModel(description = "用户查询参数")` |
| 每个字段添加说明 | ✅ | `@ApiModelProperty(value = "字段说明")` | `@ApiModelProperty(value = "用户名", example = "admin")` |
| 必填字段标注 required | 推荐 | `@ApiModelProperty(value = "说明", required = true)` | `@ApiModelProperty(value = "手机号", required = true)` |
| 提供示例值 | 推荐 | `@ApiModelProperty(value = "说明", example = "示例")` | `@ApiModelProperty(value = "状态", example = "0")` |

### 5. 返回值级别

| 检查项 | 必须 | 说明 |
|--------|:----:|------|
| 统一返回 `R<T>` | ✅ | 所有接口必须返回 `R<T>` 包装 |
| 泛型类型明确 | ✅ | 禁止使用 `R<Map>` 或 `R<Object>`，必须使用明确的 VO/DTO 类型 |
| 分页接口返回 `R<PageResult<T>>` | ✅ | 分页查询接口必须返回 `R<PageResult<具体VO类>>` |
| 返回的 VO/DTO 必须有文档注解 | ✅ | 返回对象的类和字段必须添加 `@ApiModel` 和 `@ApiModelProperty` |

---

## 验证流程

当创建或修改接口时，按以下步骤逐一验证：

### Step 1：验证 Controller 类注解
```
检查 Controller 类是否有 @Api(tags = "xxx") 注解
  ├── ✅ 有 → 继续下一步
  └── ❌ 没有 → 添加 @Api(tags = "模块名称") 并导入 io.swagger.annotations.Api
```

### Step 2：验证方法注解
```
检查每个接口方法是否有 @ApiOperation("xxx") 注解
  ├── ✅ 有 → 继续下一步
  └── ❌ 没有 → 在方法的 Mapping 注解之前添加 @ApiOperation("接口说明")
```

### Step 3：验证参数注解
```
遍历方法的所有参数：
  ├── @PathVariable 参数 → 必须有 @ApiParam(value = "说明", required = true)
  ├── @RequestParam 参数 → 必须有 @ApiParam(value = "说明")
  └── @RequestBody 参数 → 跳转到 Step 4 验证其对应的类
```

### Step 4：验证请求/响应对象
```
对于 @RequestBody 的类和返回值中的泛型类型（如 R<XxxVO> 中的 XxxVO）：
  ├── 类上必须有 @ApiModel(description = "说明")
  └── 每个字段必须有 @ApiModelProperty(value = "字段说明")
        ├── 推荐添加 example 属性
        └── 必填字段推荐添加 required = true
```

### Step 5：验证返回值
```
检查返回值类型：
  ├── 是否使用 R<T> 统一包装 → ❌ 否则修正
  ├── 泛型是否为具体类型 → ❌ Map/Object 则创建专用 VO
  └── 分页接口是否返回 R<PageResult<T>> → ❌ 否则修正
```

---

## 必须导入的包

创建接口时确保以下 import 语句存在：

```java
// Controller 类中
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;          // 如有 @PathVariable 或 @RequestParam

// DTO / VO / Query 类中
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
```

---

## 标准示例

### Controller 示例

```java
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;

    @ApiOperation("分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageResult<SysUserVO>> list(SysUserQuery query) {
        return R.ok(userService.selectUserPage(query));
    }

    @ApiOperation("根据用户ID获取详情")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{userId}")
    public R<SysUserVO> getInfo(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        return R.ok(userService.selectUserById(userId));
    }

    @ApiOperation("新增用户")
    @SaCheckPermission("system:user:add")
    @OperationLog(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysUser user) {
        userService.insertUser(user);
        return R.ok();
    }
}
```

### VO 示例

```java
@Data
@ApiModel(description = "用户视图对象")
public class SysUserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
```

### Query 示例

```java
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户查询参数")
public class SysUserQuery extends PageQuery {

    @ApiModelProperty(value = "用户名（模糊匹配）", example = "admin")
    private String username;

    @ApiModelProperty(value = "状态 (0正常 1停用)", example = "0")
    private String status;
}
```

---

## 常见遗漏场景

> [!WARNING]
> 以下场景最容易遗漏文档注解，请特别注意：

1. **内部调用接口** — 即使是 Feign 内部调用的接口，也要添加 `@ApiOperation`
2. **新增的查询参数字段** — 在 Query 类中新增字段时容易忘记 `@ApiModelProperty`
3. **返回新的 VO 类** — 创建新的返回对象时容易忘记 `@ApiModel` 和字段注解
4. **枚举值/状态码字段** — 这类字段应在 `value` 中说明可选值，如 `"状态 (0正常 1停用)"`
5. **集合类型的返回值** — 如 `R<List<XxxVO>>`，内部的 `XxxVO` 也必须有完整注解

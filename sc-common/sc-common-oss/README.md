# sc-common-oss

## 模块定位

`sc-common-oss` 封装基于 S3 协议的对象存储能力，兼容 AWS S3 与 MinIO，支持常规文件操作和 STS 临时凭证签发。

## 核心组件

- `OssConfig`：对象存储自动配置
- `OssTemplate`：统一文件上传、下载、删除等操作入口
- `OssProperties`：对象存储配置
- `StsTokenVO`：临时凭证返回对象

## 依赖方式

```xml
<dependency>
    <groupId>com.sc</groupId>
    <artifactId>sc-common-oss</artifactId>
</dependency>
```

## 配置说明

常见配置项：

```yaml
sc:
  oss:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: sc-public
```

如果要启用 STS，还需要补充角色、时长和 STS 终端地址等参数。

## 使用方式

### 服务端上传

业务服务中注入 `OssTemplate`，统一处理上传、删除和 URL 获取等动作。

### 前端直传

如果业务要实现前端直传，可在服务端签发 `StsTokenVO`，再由前端使用临时凭证直接访问对象存储。

## 适用场景

- 后台文件上传
- MinIO 兼容部署
- 云对象存储统一封装
- 前端直传

## 注意事项

- 本模块假设目标存储兼容 S3 协议。
- 生产环境中应使用独立账号、最小权限策略和专用 Bucket。

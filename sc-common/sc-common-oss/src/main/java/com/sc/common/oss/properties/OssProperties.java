package com.sc.common.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS 配置属性
 */
@Data
@ConfigurationProperties(prefix = "sc.oss")
public class OssProperties {

    /** 服务端点 */
    private String endpoint;

    /** Access Key */
    private String accessKey;

    /** Secret Key */
    private String secretKey;

    /** 存储桶名称 */
    private String bucketName;

    /** 区域 */
    private String region = "us-east-1";

    /** 是否使用路径风格访问 (MinIO 必须为 true) */
    private boolean pathStyleAccess = true;

    // ==================== STS 临时凭证配置 ====================

    /** STS 角色 ARN（如 arn:aws:iam::123456:role/oss-upload-role） */
    private String roleArn;

    /** STS 会话名称 */
    private String roleSessionName = "sc-oss-upload";

    /** STS 临时凭证有效期（秒），默认 15 分钟，最大 1 小时 */
    private int stsDurationSeconds = 900;

    /** STS 服务端点（部分私有云如 MinIO 需要自定义） */
    private String stsEndpoint;
}

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
}

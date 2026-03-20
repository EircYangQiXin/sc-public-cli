package com.sc.common.oss.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * STS 临时凭证响应
 * <p>
 * 前端拿到此凭证后，直接从浏览器上传文件到 OSS/S3 存储桶，
 * 不经过后端服务器，避免带宽堵塞。
 * </p>
 */
@Data
@ApiModel(description = "STS 临时上传凭证")
public class StsTokenVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "临时 Access Key ID")
    private String accessKeyId;

    @ApiModelProperty(value = "临时 Secret Access Key")
    private String secretAccessKey;

    @ApiModelProperty(value = "安全令牌 (Session Token)")
    private String sessionToken;

    @ApiModelProperty(value = "凭证过期时间")
    private Date expiration;

    @ApiModelProperty(value = "存储桶名称")
    private String bucketName;

    @ApiModelProperty(value = "OSS 端点")
    private String endpoint;

    @ApiModelProperty(value = "区域")
    private String region;

    @ApiModelProperty(value = "建议的上传路径前缀", example = "upload/2026/03/19/")
    private String uploadPath;
}

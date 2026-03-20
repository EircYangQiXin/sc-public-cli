package com.sc.common.oss.template;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sc.common.oss.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 对象存储操作模板
 */
@Slf4j
@RequiredArgsConstructor
public class OssTemplate {

    private final AmazonS3 amazonS3;
    private final OssProperties properties;

    /**
     * 上传文件
     *
     * @param objectKey   对象键（路径）
     * @param inputStream 文件流
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    public String upload(String objectKey, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        PutObjectRequest request = new PutObjectRequest(properties.getBucketName(), objectKey, inputStream, metadata);
        amazonS3.putObject(request);
        return getFileUrl(objectKey);
    }

    /**
     * 下载文件
     */
    public S3Object download(String objectKey) {
        return amazonS3.getObject(properties.getBucketName(), objectKey);
    }

    /**
     * 删除文件
     */
    public void delete(String objectKey) {
        amazonS3.deleteObject(properties.getBucketName(), objectKey);
    }

    /**
     * 获取文件访问 URL
     */
    public String getFileUrl(String objectKey) {
        return properties.getEndpoint() + "/" + properties.getBucketName() + "/" + objectKey;
    }

    /**
     * 获取预签名 URL（限时访问）
     *
     * @param objectKey  对象键
     * @param expiration 过期时间
     * @return 预签名 URL
     */
    public String getPresignedUrl(String objectKey, Date expiration) {
        URL url = amazonS3.generatePresignedUrl(properties.getBucketName(), objectKey, expiration);
        return url.toString();
    }

    /**
     * 检查存储桶是否存在，不存在则创建
     */
    public void ensureBucketExists() {
        String bucketName = properties.getBucketName();
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
            log.info("Created OSS bucket: {}", bucketName);
        }
    }

    /**
     * 获取 STS 临时凭证（前端直传使用）
     * <p>
     * 流程：后端通过 AssumeRole 获取临时 AK/SK/Token → 返回给前端 →
     * 前端使用临时凭证直接上传到 OSS/S3，不经过后端服务器。
     * </p>
     *
     * @param uploadPath 上传路径前缀（可选，用于限制上传目录）
     * @return STS 临时凭证
     */
    public com.sc.common.oss.domain.StsTokenVO getStsToken(String uploadPath) {
        String roleArn = properties.getRoleArn();
        if (roleArn == null || roleArn.isEmpty()) {
            throw new RuntimeException("STS roleArn 未配置，请设置 sc.oss.role-arn");
        }

        // 构建 STS 客户端
        com.amazonaws.auth.AWSStaticCredentialsProvider credentialsProvider =
                new com.amazonaws.auth.AWSStaticCredentialsProvider(
                        new com.amazonaws.auth.BasicAWSCredentials(
                                properties.getAccessKey(), properties.getSecretKey()));

        com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder stsBuilder =
                com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(credentialsProvider);

        // 自定义 STS 端点（MinIO 等私有云场景）
        if (properties.getStsEndpoint() != null && !properties.getStsEndpoint().isEmpty()) {
            stsBuilder.withEndpointConfiguration(
                    new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                            properties.getStsEndpoint(), properties.getRegion()));
        } else {
            stsBuilder.withRegion(properties.getRegion());
        }

        com.amazonaws.services.securitytoken.AWSSecurityTokenService stsClient = stsBuilder.build();

        // 构建 AssumeRole 请求
        com.amazonaws.services.securitytoken.model.AssumeRoleRequest assumeRoleRequest =
                new com.amazonaws.services.securitytoken.model.AssumeRoleRequest()
                        .withRoleArn(roleArn)
                        .withRoleSessionName(properties.getRoleSessionName())
                        .withDurationSeconds(properties.getStsDurationSeconds());

        // 可选：限制上传目录的策略
        if (uploadPath != null && !uploadPath.isEmpty()) {
            String policy = String.format(
                    "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                            + "\"Action\":[\"s3:PutObject\"],\"Resource\":\"arn:aws:s3:::%s/%s*\"}]}",
                    properties.getBucketName(), uploadPath);
            assumeRoleRequest.withPolicy(policy);
        }

        // 调用 STS
        com.amazonaws.services.securitytoken.model.AssumeRoleResult result =
                stsClient.assumeRole(assumeRoleRequest);
        com.amazonaws.services.securitytoken.model.Credentials credentials = result.getCredentials();

        // 构建返回值
        com.sc.common.oss.domain.StsTokenVO vo = new com.sc.common.oss.domain.StsTokenVO();
        vo.setAccessKeyId(credentials.getAccessKeyId());
        vo.setSecretAccessKey(credentials.getSecretAccessKey());
        vo.setSessionToken(credentials.getSessionToken());
        vo.setExpiration(credentials.getExpiration());
        vo.setBucketName(properties.getBucketName());
        vo.setEndpoint(properties.getEndpoint());
        vo.setRegion(properties.getRegion());
        vo.setUploadPath(uploadPath);

        return vo;
    }
}

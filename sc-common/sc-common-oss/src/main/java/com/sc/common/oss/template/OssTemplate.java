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
}

package com.sc.common.oss.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sc.common.oss.properties.OssProperties;
import com.sc.common.oss.template.OssTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 自动配置
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "sc.oss", name = "endpoint")
public class OssConfig {

    @Bean
    public AmazonS3 amazonS3(OssProperties properties) {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSignerOverride("AWSS3V4SignerType");

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getRegion()))
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey())))
                .withClientConfiguration(clientConfig)
                .withPathStyleAccessEnabled(properties.isPathStyleAccess())
                .build();
    }

    @Bean
    public OssTemplate ossTemplate(AmazonS3 amazonS3, OssProperties properties) {
        return new OssTemplate(amazonS3, properties);
    }
}

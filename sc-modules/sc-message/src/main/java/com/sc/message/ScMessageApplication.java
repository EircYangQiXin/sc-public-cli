package com.sc.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 站内信服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.sc")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.sc.api")
@EnableAsync
public class ScMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScMessageApplication.class, args);
        System.out.println("============ SC Message Server Started ============");
    }
}

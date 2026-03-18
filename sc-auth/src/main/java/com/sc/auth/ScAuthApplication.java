package com.sc.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.sc")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.sc.api")
public class ScAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScAuthApplication.class, args);
        System.out.println("============ SC Auth Server Started ============");
    }
}

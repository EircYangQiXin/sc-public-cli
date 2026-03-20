package com.sc.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统管理服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.sc")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.sc.api")
@EnableAsync
public class ScSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScSystemApplication.class, args);
        System.out.println("============ SC System Server Started ============");
    }
}

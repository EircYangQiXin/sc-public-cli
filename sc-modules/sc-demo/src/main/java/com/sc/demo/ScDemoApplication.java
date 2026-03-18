package com.sc.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 示例服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.sc")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.sc.api")
public class ScDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScDemoApplication.class, args);
        System.out.println("============ SC Demo Server Started ============");
    }
}

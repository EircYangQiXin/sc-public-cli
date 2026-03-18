package com.sc.demo.controller;

import com.sc.api.system.RemoteUserService;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 示例控制器 - 演示各中间件集成用法
 */
@Api(tags = "示例接口")
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final RemoteUserService remoteUserService;
    private final RabbitTemplate rabbitTemplate;

    @ApiOperation("Feign 远程调用示例")
    @GetMapping("/feign/{username}")
    public R<SysUserDTO> feignDemo(@PathVariable String username) {
        return remoteUserService.getUserInfo(username);
    }

    @ApiOperation("RabbitMQ 发送消息示例")
    @PostMapping("/mq/send")
    public R<Void> sendMessage(@RequestParam String message) {
        rabbitTemplate.convertAndSend("demo.exchange", "demo.routing.key", message);
        return R.ok("消息发送成功", null);
    }

    @ApiOperation("健康检测")
    @GetMapping("/health")
    public R<String> health() {
        return R.ok("Demo Service is running!");
    }
}

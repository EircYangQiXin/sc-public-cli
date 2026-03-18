package com.sc.demo.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例定时任务 (XXL-JOB)
 */
@Slf4j
@Component
public class DemoJobHandler {

    /**
     * 示例任务 - 在 XXL-JOB Admin 中配置 JobHandler 为 "demoJobHandler"
     */
    @XxlJob("demoJobHandler")
    public void demoJob() {
        log.info("===> 示例定时任务执行 <===");
        // TODO: 在这里编写定时任务逻辑
    }
}

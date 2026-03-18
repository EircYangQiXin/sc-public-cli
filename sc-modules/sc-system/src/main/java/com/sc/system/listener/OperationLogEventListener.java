package com.sc.system.listener;

import cn.hutool.core.bean.BeanUtil;
import com.sc.common.log.event.OperationLogEvent;
import com.sc.system.domain.entity.SysOperLog;
import com.sc.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器（异步持久化）
 * <p>
 * 设计模式：观察者模式 (Spring Event)
 * AOP 切面发布 OperationLogEvent → 本监听器异步写入数据库
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogEventListener {

    private final SysOperLogMapper operLogMapper;

    @Async
    @EventListener
    public void onOperationLog(OperationLogEvent event) {
        try {
            SysOperLog operLog = new SysOperLog();
            BeanUtil.copyProperties(event, operLog);
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("操作日志持久化失败", e);
        }
    }
}

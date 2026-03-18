package com.sc.system.listener;

import cn.hutool.core.bean.BeanUtil;
import com.sc.common.log.event.LoginLogEvent;
import com.sc.system.domain.entity.SysLoginLog;
import com.sc.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件监听器（异步持久化）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogEventListener {

    private final SysLoginLogMapper loginLogMapper;

    @Async
    @EventListener
    public void onLoginLog(LoginLogEvent event) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            BeanUtil.copyProperties(event, loginLog);
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("登录日志持久化失败", e);
        }
    }
}

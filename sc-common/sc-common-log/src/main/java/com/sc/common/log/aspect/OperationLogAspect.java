package com.sc.common.log.aspect;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.event.OperationLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    /**
     * 正常返回
     */
    @AfterReturning(pointcut = "@annotation(operationLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, OperationLog operationLog, Object result) {
        handleLog(joinPoint, operationLog, null, result);
    }

    /**
     * 异常抛出
     */
    @AfterThrowing(pointcut = "@annotation(operationLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperationLog operationLog, Exception e) {
        handleLog(joinPoint, operationLog, e, null);
    }

    private void handleLog(JoinPoint joinPoint, OperationLog operationLog, Exception e, Object result) {
        try {
            OperationLogEvent event = new OperationLogEvent();
            event.setTitle(operationLog.title());
            event.setBusinessType(operationLog.businessType().ordinal());
            event.setOperatorType(operationLog.operatorType().ordinal());
            event.setOperName(SecurityContextHolder.getUsername());
            event.setOperTime(LocalDateTime.now());

            // 记录方法名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            event.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName() + "()");

            // 请求参数
            if (operationLog.isSaveRequestData()) {
                setRequestParams(joinPoint, event);
            }

            // 响应数据
            if (operationLog.isSaveResponseData() && result != null) {
                String responseStr = objectMapper.writeValueAsString(result);
                event.setJsonResult(StrUtil.sub(responseStr, 0, 2000));
            }

            // 异常信息
            if (e != null) {
                event.setStatus(1); // 异常
                event.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            } else {
                event.setStatus(0); // 正常
            }

            // 通过事件异步存储
            publisher.publishEvent(event);
        } catch (Exception ex) {
            log.error("记录操作日志异常", ex);
        }
    }

    private void setRequestParams(JoinPoint joinPoint, OperationLogEvent event) {
        try {
            Object[] args = joinPoint.getArgs();
            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    continue;
                }
                params.append(objectMapper.writeValueAsString(arg)).append(" ");
            }
            event.setOperParam(StrUtil.sub(params.toString().trim(), 0, 2000));
        } catch (Exception e) {
            // ignore
        }
    }
}

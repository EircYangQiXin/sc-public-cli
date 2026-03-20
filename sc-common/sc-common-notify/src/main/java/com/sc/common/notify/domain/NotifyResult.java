package com.sc.common.notify.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知发送结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    public static NotifyResult ok() {
        return new NotifyResult(true, "发送成功");
    }

    public static NotifyResult ok(String message) {
        return new NotifyResult(true, message);
    }

    public static NotifyResult fail(String message) {
        return new NotifyResult(false, message);
    }
}

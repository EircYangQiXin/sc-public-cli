package com.sc.common.notify.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /**
     * 处理失败的接收人列表（部分成功时返回）
     */
    private List<String> failedReceivers;

    public NotifyResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static NotifyResult ok() {
        return new NotifyResult(true, "发送成功");
    }

    public static NotifyResult ok(String message) {
        return new NotifyResult(true, message);
    }

    public static NotifyResult fail(String message) {
        return new NotifyResult(false, message);
    }

    /**
     * 部分成功：有效接收人已入库，但存在无法解析的接收人
     */
    public static NotifyResult partial(String message, List<String> failedReceivers) {
        NotifyResult result = new NotifyResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setFailedReceivers(failedReceivers);
        return result;
    }

    /**
     * 是否存在部分失败
     */
    public boolean hasPartialFailure() {
        return failedReceivers != null && !failedReceivers.isEmpty();
    }
}

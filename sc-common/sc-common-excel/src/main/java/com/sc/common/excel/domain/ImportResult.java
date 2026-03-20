package com.sc.common.excel.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 */
@Data
public class ImportResult {

    /**
     * 总行数
     */
    private int totalCount;

    /**
     * 成功行数
     */
    private int successCount;

    /**
     * 失败行数
     */
    private int errorCount;

    /**
     * 错误详情列表
     */
    private List<ErrorDetail> errors = new ArrayList<>();

    public void addError(int rowIndex, String message) {
        errors.add(new ErrorDetail(rowIndex, message));
    }

    /**
     * 错误详情
     */
    @Data
    public static class ErrorDetail {
        private final int rowIndex;
        private final String message;
    }
}

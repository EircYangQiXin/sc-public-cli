package com.sc.common.excel.handler;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.sc.common.excel.domain.ImportResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入数据监听器抽象基类（模板方法模式）
 * <p>
 * 子类只需实现 {@link #validate} 和 {@link #doSave} 方法。
 * 框架自动处理攒批、校验、入库、结果统计。
 * </p>
 *
 * <pre>
 * public class UserImportListener extends AbstractImportListener&lt;UserImportDTO&gt; {
 *     &#64;Override
 *     protected String validate(UserImportDTO data, int rowIndex) {
 *         if (StrUtil.isBlank(data.getUsername())) {
 *             return "用户名不能为空";
 *         }
 *         return null;
 *     }
 *
 *     &#64;Override
 *     protected void doSave(List&lt;UserImportDTO&gt; batch) {
 *         userService.saveBatch(batch);
 *     }
 * }
 * </pre>
 *
 * @param <T> 导入数据类型
 */
@Slf4j
public abstract class AbstractImportListener<T> extends AnalysisEventListener<T> {

    /**
     * 每批入库的行数，默认 500
     */
    private static final int DEFAULT_BATCH_SIZE = 500;

    private final int batchSize;

    private final List<T> batchList = new ArrayList<>();

    @Getter
    private final ImportResult result = new ImportResult();

    private int dataCount = 0;

    public AbstractImportListener() {
        this(DEFAULT_BATCH_SIZE);
    }

    public AbstractImportListener(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 使用 EasyExcel 上下文获取真实 Excel 行号（0-based），+1 转为用户友好的 1-based
        int excelRowIndex = context.readRowHolder().getRowIndex() + 1;
        dataCount++;
        result.setTotalCount(result.getTotalCount() + 1);

        // 逐行校验
        String errorMsg = validate(data, excelRowIndex);
        if (errorMsg != null) {
            result.setErrorCount(result.getErrorCount() + 1);
            result.addError(excelRowIndex, errorMsg);
            return;
        }

        batchList.add(data);

        // 达到批次大小，执行入库
        if (batchList.size() >= batchSize) {
            saveBatch(excelRowIndex);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理最后一批
        if (!batchList.isEmpty()) {
            // 最后一批使用上下文中的最后处理行号
            int lastRow = context.readRowHolder() != null
                    ? context.readRowHolder().getRowIndex() + 1
                    : dataCount;
            saveBatch(lastRow);
        }
        log.info("导入完成 → 总行数={}, 成功={}, 失败={}",
                result.getTotalCount(), result.getSuccessCount(), result.getErrorCount());
    }

    private void saveBatch(int lastRowIndex) {
        try {
            doSave(batchList);
            result.setSuccessCount(result.getSuccessCount() + batchList.size());
        } catch (Exception e) {
            int firstRowIndex = lastRowIndex - batchList.size() + 1;
            log.error("批量入库失败, Excel行范围=[{}-{}], error={}",
                    firstRowIndex, lastRowIndex, e.getMessage(), e);
            result.setErrorCount(result.getErrorCount() + batchList.size());
            result.addError(lastRowIndex, "批量入库失败: " + e.getMessage());
        } finally {
            batchList.clear();
        }
    }

    /**
     * 逐行校验数据
     *
     * @param data     当前行数据
     * @param rowIndex 行号（从 1 开始）
     * @return 校验失败返回错误信息，成功返回 null
     */
    protected abstract String validate(T data, int rowIndex);

    /**
     * 批量入库
     *
     * @param batch 一批校验通过的数据
     */
    protected abstract void doSave(List<T> batch);
}

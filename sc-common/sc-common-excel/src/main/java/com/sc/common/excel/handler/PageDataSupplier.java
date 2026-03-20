package com.sc.common.excel.handler;

import java.util.List;

/**
 * 分页数据提供者函数式接口
 * <p>
 * 用于分页流式导出场景，避免一次性加载全量数据导致 OOM。
 * </p>
 *
 * <pre>
 * ExcelHelper.exportByPage(response, "用户列表", UserExportVO.class,
 *     (pageNo, pageSize) -&gt; userService.listPage(pageNo, pageSize));
 * </pre>
 *
 * @param <T> 数据类型
 */
@FunctionalInterface
public interface PageDataSupplier<T> {

    /**
     * 根据页码和页大小获取一页数据
     *
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 当前页数据，返回空集合或 null 表示无更多数据
     */
    List<T> getData(int pageNo, int pageSize);
}

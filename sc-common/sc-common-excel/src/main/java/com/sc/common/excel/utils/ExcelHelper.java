package com.sc.common.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.sc.common.excel.domain.ImportResult;
import com.sc.common.excel.handler.AbstractImportListener;
import com.sc.common.excel.handler.PageDataSupplier;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Excel / CSV 导入导出工具类
 * <p>
 * 基于 EasyExcel 封装，提供同步导出、分页流式导出和导入三种核心能力。
 * </p>
 */
@Slf4j
public final class ExcelHelper {

    /**
     * 分页导出时每页查询的默认大小
     */
    private static final int DEFAULT_PAGE_SIZE = 5000;

    private ExcelHelper() {
    }

    // ========================= 导出 =========================

    /**
     * 同步导出 Excel（适合 5000 行以内的小数据量）
     *
     * @param response HTTP 响应对象
     * @param fileName 文件名（不含扩展名）
     * @param data     数据列表
     * @param clazz    数据类型（EasyExcel 注解驱动）
     * @param <T>      数据泛型
     */
    public static <T> void export(HttpServletResponse response,
                                  String fileName,
                                  List<T> data,
                                  Class<T> clazz) {
        try {
            setExcelResponseHeader(response, fileName);
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet("Sheet1")
                    .doWrite(data);
        } catch (IOException e) {
            log.error("Excel 导出失败, fileName={}", fileName, e);
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    /**
     * 分页流式导出 Excel（适合大数据量，避免 OOM）
     *
     * @param response HTTP 响应对象
     * @param fileName 文件名（不含扩展名）
     * @param clazz    数据类型
     * @param supplier 分页数据提供者
     * @param <T>      数据泛型
     */
    public static <T> void exportByPage(HttpServletResponse response,
                                        String fileName,
                                        Class<T> clazz,
                                        PageDataSupplier<T> supplier) {
        exportByPage(response, fileName, clazz, supplier, DEFAULT_PAGE_SIZE);
    }

    /**
     * 分页流式导出 Excel（自定义页大小）
     *
     * @param response HTTP 响应对象
     * @param fileName 文件名（不含扩展名）
     * @param clazz    数据类型
     * @param supplier 分页数据提供者
     * @param pageSize 每页大小
     * @param <T>      数据泛型
     */
    public static <T> void exportByPage(HttpServletResponse response,
                                        String fileName,
                                        Class<T> clazz,
                                        PageDataSupplier<T> supplier,
                                        int pageSize) {
        ExcelWriter writer = null;
        try {
            setExcelResponseHeader(response, fileName);
            writer = EasyExcel.write(response.getOutputStream(), clazz).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();

            int pageNo = 1;
            while (true) {
                List<T> data = supplier.getData(pageNo, pageSize);
                if (data == null || data.isEmpty()) {
                    break;
                }
                writer.write(data, writeSheet);
                pageNo++;
            }
        } catch (IOException e) {
            log.error("Excel 分页导出失败, fileName={}", fileName, e);
            throw new RuntimeException("Excel 导出失败", e);
        } finally {
            if (writer != null) {
                writer.finish();
            }
        }
    }

    /**
     * 导出 CSV 文件（基于 EasyExcel 的 CSV 写入）
     *
     * @param response HTTP 响应对象
     * @param fileName 文件名（不含扩展名）
     * @param data     数据列表
     * @param clazz    数据类型
     * @param <T>      数据泛型
     */
    public static <T> void exportCsv(HttpServletResponse response,
                                     String fileName,
                                     List<T> data,
                                     Class<T> clazz) {
        try {
            setCsvResponseHeader(response, fileName);
            OutputStream os = response.getOutputStream();
            // 写入 UTF-8 BOM，兼容 Excel 打开 CSV 中文不乱码
            os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            EasyExcel.write(os, clazz)
                    .excelType(com.alibaba.excel.support.ExcelTypeEnum.CSV)
                    .sheet("Sheet1")
                    .doWrite(data);
        } catch (IOException e) {
            log.error("CSV 导出失败, fileName={}", fileName, e);
            throw new RuntimeException("CSV 导出失败", e);
        }
    }

    // ========================= 导入 =========================

    /**
     * 同步导入 Excel
     *
     * @param inputStream 文件输入流
     * @param clazz       数据类型
     * @param listener    导入监听器（继承 AbstractImportListener）
     * @param <T>         数据泛型
     * @return 导入结果
     */
    public static <T> ImportResult importExcel(InputStream inputStream,
                                               Class<T> clazz,
                                               AbstractImportListener<T> listener) {
        EasyExcel.read(inputStream, clazz, listener).sheet().doRead();
        return listener.getResult();
    }

    /**
     * 同步导入 CSV
     *
     * @param inputStream 文件输入流
     * @param clazz       数据类型
     * @param listener    导入监听器
     * @param <T>         数据泛型
     * @return 导入结果
     */
    public static <T> ImportResult importCsv(InputStream inputStream,
                                             Class<T> clazz,
                                             AbstractImportListener<T> listener) {
        EasyExcel.read(inputStream, clazz, listener)
                .excelType(com.alibaba.excel.support.ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return listener.getResult();
    }

    // ========================= 辅助方法 =========================

    private static void setExcelResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName + ".xlsx");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("文件名编码失败", e);
        }
    }

    private static void setCsvResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv");
        response.setCharacterEncoding("utf-8");
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName + ".csv");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("文件名编码失败", e);
        }
    }
}

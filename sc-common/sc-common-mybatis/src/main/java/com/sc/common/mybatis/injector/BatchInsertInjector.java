package com.sc.common.mybatis.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义 SQL 注入器 — 批量插入优化
 * <p>
 * MyBatis-Plus 默认 {@code saveBatch()} 是逐条 INSERT，
 * 本注入器注册真正的批量 INSERT INTO ... VALUES (...), (...) 语句，
 * 大数据量时性能提升显著。
 * </p>
 * <p>
 * 使用方式：在 Mapper 中定义 {@code int insertBatchSomeColumn(List entity)}
 * </p>
 */
@Component
public class BatchInsertInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        // 添加批量插入方法，自动忽略逻辑删除字段
        methodList.add(new InsertBatchSomeColumn(i -> !i.isLogicDelete()));
        return methodList;
    }
}

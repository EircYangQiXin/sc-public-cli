package com.sc.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.domain.model.LoginUser;
import com.sc.common.mybatis.annotation.DataScope;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * <p>
 * 数据范围：
 * 1 = 全部数据权限
 * 2 = 自定义部门数据权限
 * 3 = 本部门数据权限
 * 4 = 本部门及以下数据权限
 * 5 = 仅本人数据权限
 * </p>
 */
@Slf4j
public class DataPermissionInterceptor implements InnerInterceptor {

    /** 全部数据权限 */
    public static final int DATA_SCOPE_ALL = 1;
    /** 自定义部门 */
    public static final int DATA_SCOPE_CUSTOM = 2;
    /** 本部门 */
    public static final int DATA_SCOPE_DEPT = 3;
    /** 本部门及以下 */
    public static final int DATA_SCOPE_DEPT_AND_CHILD = 4;
    /** 仅本人 */
    public static final int DATA_SCOPE_SELF = 5;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 查找 Mapper 方法上的 @DataScope 注解
        DataScope dataScope = getDataScope(ms);
        if (dataScope == null) {
            return;
        }

        LoginUser loginUser = SecurityContextHolder.get("loginUser", LoginUser.class);
        if (loginUser == null) {
            return;
        }

        // 超级管理员不做数据过滤
        if (loginUser.getRoles() != null && loginUser.getRoles().contains("admin")) {
            return;
        }

        String sqlCondition = buildDataScopeCondition(dataScope, loginUser);
        if (sqlCondition == null || sqlCondition.isEmpty()) {
            return;
        }

        // 拼接原始 SQL
        try {
            String originalSql = boundSql.getSql();
            Expression where = CCJSqlParserUtil.parseCondExpression(sqlCondition);
            // 此处通过反射修改 BoundSql 中的 SQL
            String newSql = originalSql + " AND " + sqlCondition;
            java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, newSql);
        } catch (Exception e) {
            log.error("数据权限SQL拼接失败", e);
        }
    }

    /**
     * 根据数据权限范围构建 SQL 条件
     */
    private String buildDataScopeCondition(DataScope dataScope, LoginUser loginUser) {
        String deptAlias = dataScope.deptAlias();
        String userAlias = dataScope.userAlias();
        Integer scope = loginUser.getDataScope();

        if (scope == null || scope == DATA_SCOPE_ALL) {
            return null;
        }

        switch (scope) {
            case DATA_SCOPE_CUSTOM:
                // 自定义部门
                Set<Long> deptIds = loginUser.getDataScopeDeptIds();
                if (deptIds != null && !deptIds.isEmpty()) {
                    String ids = deptIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                    return deptAlias + ".dept_id IN (" + ids + ")";
                }
                return "1=0";
            case DATA_SCOPE_DEPT:
                // 本部门
                return deptAlias + ".dept_id = " + loginUser.getDeptId();
            case DATA_SCOPE_DEPT_AND_CHILD:
                // 本部门及子部门（通过子查询）
                return deptAlias + ".dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = "
                        + loginUser.getDeptId() + " OR FIND_IN_SET(" + loginUser.getDeptId() + ", ancestors))";
            case DATA_SCOPE_SELF:
                // 仅本人
                return userAlias + ".create_by = '" + loginUser.getUsername() + "'";
            default:
                return null;
        }
    }

    /**
     * 获取 Mapper 方法上的 @DataScope 注解
     */
    private DataScope getDataScope(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> clazz = Class.forName(className);
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}

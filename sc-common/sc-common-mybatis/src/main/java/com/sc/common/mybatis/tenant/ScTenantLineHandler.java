package com.sc.common.mybatis.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.domain.model.LoginUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * 多租户处理器
 */
public class ScTenantLineHandler implements TenantLineHandler {

    private final TenantProperties properties;

    public ScTenantLineHandler(TenantProperties properties) {
        this.properties = properties;
    }

    @Override
    public Expression getTenantId() {
        LoginUser loginUser = SecurityContextHolder.get("loginUser", LoginUser.class);
        if (loginUser != null && loginUser.getTenantId() != null) {
            return new LongValue(loginUser.getTenantId());
        }
        return new NullValue();
    }

    @Override
    public String getTenantIdColumn() {
        return properties.getColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return properties.getIgnoreTables().contains(tableName);
    }
}

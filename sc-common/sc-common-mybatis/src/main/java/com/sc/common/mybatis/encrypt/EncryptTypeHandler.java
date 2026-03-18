package com.sc.common.mybatis.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 敏感字段加解密 TypeHandler
 * <p>
 * 作为 MyBatis TypeHandler，在 SQL 参数设置时自动加密，
 * 在结果集读取时自动解密。
 * <p>
 * 使用方式（在 Entity 字段上）：
 * <pre>
 * {@code
 * @TableField(typeHandler = EncryptTypeHandler.class)
 * private String phone;
 * }
 * </pre>
 * <p>
 * 同时需要在 Mapper XML 或 @TableName 注解中开启 resultMap 自动映射：
 * {@code @TableName(value = "table_name", autoResultMap = true)}
 * </p>
 */
@Slf4j
@Component
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static EncryptStrategy encryptStrategy;

    /**
     * 静态注入（Spring 管理的 Bean 注入到静态字段）
     */
    public EncryptTypeHandler(EncryptStrategy encryptStrategy) {
        EncryptTypeHandler.encryptStrategy = encryptStrategy;
    }

    /**
     * MyBatis 反射创建时的无参构造
     */
    public EncryptTypeHandler() {
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (encryptStrategy != null) {
            ps.setString(i, encryptStrategy.encrypt(parameter));
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    private String decrypt(String value) {
        if (value != null && encryptStrategy != null) {
            return encryptStrategy.decrypt(value);
        }
        return value;
    }
}

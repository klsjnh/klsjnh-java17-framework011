package com.klsjnh.common.util;

/*                JdbcUrlGuard011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  jdbc url guard unit test (scheme whitelist + forbidden params)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link JdbcUrlGuard011}: built-in scheme prefixes and
 * forbidden connection parameters.
 */

class JdbcUrlGuard011Test {

    /**
     * Built-in types must use their own scheme prefix.
     */
    @Test
    void builtInSchemeIsEnforced() {
        assertDoesNotThrow(() -> JdbcUrlGuard011.assertAllowed("mysql", "jdbc:mysql://127.0.0.1:3306/db"));
        assertDoesNotThrow(() -> JdbcUrlGuard011.assertAllowed("oracle", "jdbc:oracle:thin:@host:1521:sid"));
        assertDoesNotThrow(() -> JdbcUrlGuard011.assertAllowed("sqlserver", "jdbc:sqlserver://host:1433;databaseName=x"));
        assertDoesNotThrow(() -> JdbcUrlGuard011.assertAllowed("postgres", "jdbc:postgresql://host:5432/db"));
    }

    /**
     * A cross-type URL (scheme of another database) is rejected.
     */
    @Test
    void crossTypeUrlIsRejected() {
        assertThrows(BusinessException.class,
                () -> JdbcUrlGuard011.assertAllowed("mysql", "jdbc:oracle:thin:@host:1521:sid"));
    }

    /**
     * Custom dialect types keep the bare {@code jdbc:} rule.
     */
    @Test
    void customTypeKeepsBareJdbcRule() {
        assertDoesNotThrow(() -> JdbcUrlGuard011.assertAllowed("clickhouse", "jdbc:clickhouse://host:8123/db"));
    }

    /**
     * Forbidden connection parameters are rejected, case-insensitively.
     */
    @Test
    void forbiddenParametersAreRejected() {
        assertThrows(BusinessException.class,
                () -> JdbcUrlGuard011.assertAllowed("mysql", "jdbc:mysql://host/db?allowLoadLocalInfile=true"));

        assertThrows(BusinessException.class,
                () -> JdbcUrlGuard011.assertAllowed("mysql", "jdbc:mysql://host/db?autoDeserialize=true"));

        assertThrows(BusinessException.class, () -> JdbcUrlGuard011.assertAllowed("mysql",
                "jdbc:mysql://host/db?queryInterceptors=com.evil.Evil&statementInterceptors=com.evil.Evil2"));

        assertThrows(BusinessException.class,
                () -> JdbcUrlGuard011.assertAllowed("mysql", "jdbc:mysql://host/db?allowUrlInLocalInfile=true"));
    }

    /**
     * A blank URL is rejected.
     */
    @Test
    void blankUrlIsRejected() {
        assertThrows(BusinessException.class, () -> JdbcUrlGuard011.assertAllowed("mysql", " "));
    }
}

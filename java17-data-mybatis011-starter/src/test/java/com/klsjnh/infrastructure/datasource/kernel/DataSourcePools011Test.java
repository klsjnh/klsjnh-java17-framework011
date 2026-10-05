package com.klsjnh.infrastructure.datasource.kernel;

/*                DataSourcePools011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  pool build guard unit test (url guard + driver lock)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.alibaba.druid.pool.DruidDataSource;

/**
 * Unit tests for the pool build guard in {@link DataSourcePools011}: the JDBC
 * URL guard and the built-in driver lock run before any pool object is
 * allocated, and custom dialect types keep their explicit driver.
 */

class DataSourcePools011Test {

    /**
     * Pool builder under test.
     */
    private final DataSourcePools011 pools = new DataSourcePools011();

    /**
     * A built-in type with the default driver builds a (probe) pool.
     */
    @Test
    void builtInTypeWithDefaultDriverBuilds() {
        DruidDataSource pool = pools.buildForProbe(info("mysql", "jdbc:mysql://127.0.0.1:3306/db", null));

        pool.close();
    }

    /**
     * A built-in type with a foreign driverClass is rejected — an arbitrary
     * class name must never reach the pool's driver loading.
     */
    @Test
    void builtInTypeRejectsForeignDriver() {
        assertThrows(BusinessException.class,
                () -> pools.buildForProbe(info("mysql", "jdbc:mysql://127.0.0.1:3306/db", "com.evil.Driver")));
    }

    /**
     * A URL not matching the type scheme is rejected before allocation.
     */
    @Test
    void schemeMismatchIsRejected() {
        assertThrows(BusinessException.class,
                () -> pools.buildForProbe(info("mysql", "jdbc:oracle:thin:@host:1521:sid", null)));
    }

    /**
     * A forbidden connection parameter is rejected before allocation.
     */
    @Test
    void forbiddenParameterIsRejected() {
        assertThrows(BusinessException.class, () -> pools.buildForProbe(
                info("mysql", "jdbc:mysql://host/db?allowLoadLocalInfile=true", null)));
    }

    /**
     * A custom dialect type may carry its own driver class.
     */
    @Test
    void customTypeAcceptsExplicitDriver() {
        DruidDataSource pool = pools.buildForProbe(
                info("clickhouse", "jdbc:clickhouse://host:8123/db", "com.clickhouse.jdbc.ClickHouseDriver"));

        pool.close();
    }

    /**
     * Sample connection info.
     *
     * @param type        database type
     * @param url         jdbc url
     * @param driverClass explicit driver class, nullable
     * @return connection info
     */
    private ConnectionInfo info(String type, String url, String driverClass) {
        return new ConnectionInfo("dsA", "Data source A", type, url, "u", "p", null, driverClass, null);
    }
}

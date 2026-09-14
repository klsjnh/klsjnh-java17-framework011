package com.klsjnh.infrastructure.datasource;

/*                SqlRoutingExecutor class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  sql routing executor class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.datasource.ConnectionInfo;
import com.klsjnh.domain.datasource.SqlRoutingPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * SQL routing executor: a private JdbcTemplate over the routing datasource
 * (default target = the primary datasource, dynamic targets resolved by
 * dsCode). Pools connect lazily — ensurePool runs before every routed call.
 * <p>
 * Page size is clamped to [10, 500] in code (product decision).
 * </p>
 */

@Component
public class SqlRoutingExecutor implements SqlRoutingPort {

    /**
     * Page size lower bound (product decision, code-enforced).
     */
    private static final int PAGE_SIZE_MIN = 10;

    /**
     * Page size upper bound (product decision, code-enforced).
     */
    private static final int PAGE_SIZE_MAX = 500;

    /**
     * Registry (config declaration + lazy pool ensure).
     */
    private final DynamicDataSourceRegistryImpl registry;

    /**
     * Dialect strategy.
     */
    private final SqlDialect011 dialect;

    /**
     * JDBC template over the routing datasource.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Create the executor; the routing datasource is built in code (never a
     * bean) with the primary datasource as its default target.
     *
     * @param primaryDataSource the auto-configured primary datasource
     * @param registry          dynamic datasource registry
     * @param dialect           pagination dialect
     */
    public SqlRoutingExecutor(DataSource primaryDataSource, DynamicDataSourceRegistryImpl registry,
            SqlDialect011 dialect) {
        this.registry = registry;
        this.dialect = dialect;
        this.jdbcTemplate = new JdbcTemplate(new DynamicDataSource011(primaryDataSource));
    }

    /**
     * Select rows from a dynamic datasource.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored select statement
     * @return rows as column-value maps
     */
    @Override
    public List<Map<String, Object>> selectList(String dsCode, String sql) {
        registry.ensurePool(dsCode);

        try {
            DynamicDataSource011.set(dsCode);

            return jdbcTemplate.queryForList(sql);
        } finally {
            DynamicDataSource011.clear();
        }
    }

    /**
     * Select a single row from a dynamic datasource.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored select statement
     * @return first row or null
     */
    @Override
    public Map<String, Object> selectOne(String dsCode, String sql) {
        List<Map<String, Object>> rows = selectList(dsCode, sql);

        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Execute an update / insert / delete / DDL statement.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored statement
     * @return affected row count
     */
    @Override
    public int execute(String dsCode, String sql) {
        registry.ensurePool(dsCode);

        try {
            DynamicDataSource011.set(dsCode);

            return jdbcTemplate.update(sql);
        } finally {
            DynamicDataSource011.clear();
        }
    }

    /**
     * Scalar string read: first column of the first row.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored scalar select
     * @return first column value or null
     */
    @Override
    public String selectStr(String dsCode, String sql) {
        Map<String, Object> row = selectOne(dsCode, sql);

        if (row == null || row.isEmpty()) {
            return null;
        }

        Object value = row.values().iterator().next();

        return value == null ? null : String.valueOf(value);
    }

    /**
     * Scalar integer read: first column of the first row, parsed via
     * BigDecimal ("5" / "5.0" both fine; blank → null).
     *
     * @param dsCode datasource code
     * @param sql    developer-authored scalar select
     * @return first column value or null
     */
    @Override
    public Integer selectInt(String dsCode, String sql) {
        String value = selectStr(dsCode, sql);

        if (value == null || value.isBlank()) {
            return null;
        }

        return new java.math.BigDecimal(value).intValue();
    }

    /**
     * Paged select with database dialect and a page size clamped to
     * [10, 500] in code.
     *
     * @param dsCode    datasource code
     * @param sql       developer-authored select statement
     * @param pageIndex page index starting at 1
     * @param pageSize  requested page size, clamped
     * @return page result
     */
    @Override
    public PageResult011<Map<String, Object>> selectListByPage(String dsCode, String sql, Integer pageIndex,
            Integer pageSize) {
        registry.ensurePool(dsCode);

        int size = clamp(pageSize);
        int index = pageIndex == null || pageIndex < 1 ? 1 : pageIndex;
        long offset = (long) (index - 1) * size;

        ConnectionInfo info = registry.getConfig(dsCode);

        try {
            DynamicDataSource011.set(dsCode);

            List<Map<String, Object>> rows = jdbcTemplate
                    .queryForList(dialect.pageSql(info.dsType(), sql, offset, size));
            Long total = jdbcTemplate.queryForObject(dialect.countSql(sql), Long.class);

            return PageResult011.of(new PageQuery011(index, size), total == null ? 0 : total, rows);
        } finally {
            DynamicDataSource011.clear();
        }
    }

    /**
     * Clamp a requested page size into [10, 500].
     *
     * @param pageSize requested size
     * @return clamped size
     */
    private int clamp(Integer pageSize) {
        int value = pageSize == null ? PAGE_SIZE_MIN : pageSize;

        return Math.max(PAGE_SIZE_MIN, Math.min(PAGE_SIZE_MAX, value));
    }
}

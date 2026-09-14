package com.klsjnh.domain.datasource;

/*                SqlRoutingPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  sql routing port interface
 *
 */

import com.klsjnh.common.page.PageResult011;

import java.util.List;
import java.util.Map;

/**
 * SQL routing port: executes developer-provided SQL against a dynamic
 * datasource by dsCode (read and write). The SQL is authored in server-side
 * code — never user input passed through (internal data-service scenario).
 */

public interface SqlRoutingPort {

    /**
     * Select rows from a dynamic datasource.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored select statement
     * @return rows as column-value maps
     */
    List<Map<String, Object>> selectList(String dsCode, String sql);

    /**
     * Select a single row from a dynamic datasource.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored select statement
     * @return first row or null
     */
    Map<String, Object> selectOne(String dsCode, String sql);

    /**
     * Execute an update / insert / delete / DDL statement.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored statement
     * @return affected row count
     */
    int execute(String dsCode, String sql);

    /**
     * Scalar string read: the FIRST COLUMN of the first row as a string
     * (no alias convention needed — SELECT COUNT(*) works as-is). Null when
     * no rows or the value is null.
     *
     * @param dsCode datasource code
     * @param sql    developer-authored scalar select
     * @return first column value or null
     */
    String selectStr(String dsCode, String sql);

    /**
     * Scalar integer read: the FIRST COLUMN of the first row as an integer
     * (blank → null; "5" / "5.0" both parse via BigDecimal).
     *
     * @param dsCode datasource code
     * @param sql    developer-authored scalar select
     * @return first column value or null
     */
    Integer selectInt(String dsCode, String sql);

    /**
     * Paged select with database dialect and a page size clamped to
     * [10, 500] in code (product decision).
     *
     * @param dsCode    datasource code
     * @param sql       developer-authored select statement (ORDER BY recommended)
     * @param pageIndex page index starting at 1
     * @param pageSize  page size, clamped to [10, 500]
     * @return page result
     */
    PageResult011<Map<String, Object>> selectListByPage(String dsCode, String sql, Integer pageIndex,
            Integer pageSize);
}

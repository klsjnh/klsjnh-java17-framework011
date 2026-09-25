package com.klsjnh.infrastructure.datasource.kernel.dialect;

/*                PostgresqlDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  postgresql pagination dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.kernel.SqlDialectPort011;

import org.springframework.stereotype.Component;

/**
 * PostgreSQL pagination dialect ({@code LIMIT ... OFFSET ...}).
 */

@Component
public class PostgresqlDialect011 implements SqlDialectPort011 {

    @Override
    public String dbType() {
        return DatabaseTypes011.POSTGRESQL;
    }

    @Override
    public String pageSql(String sql, long offset, int pageSize) {
        return "SELECT * FROM ( " + sql + " ) klsjnh_page LIMIT " + pageSize + " OFFSET " + offset;
    }
}

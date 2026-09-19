package com.klsjnh.infrastructure.datasource.kernel.dialect;

/*                SqlserverDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  sqlserver pagination dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.kernel.SqlDialectPort011;

import org.springframework.stereotype.Component;

/**
 * SQL Server pagination dialect ({@code OFFSET ... ROWS FETCH NEXT ...}).
 */

@Component
public class SqlserverDialect011 implements SqlDialectPort011 {

    @Override
    public String dbType() {
        return DatabaseTypes011.SQLSERVER;
    }

    @Override
    public String pageSql(String sql, long offset, int pageSize) {
        return "SELECT * FROM ( " + sql + " ) klsjnh_page OFFSET " + offset + " ROWS FETCH NEXT " + pageSize
                + " ROWS ONLY";
    }
}

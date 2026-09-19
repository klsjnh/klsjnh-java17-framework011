package com.klsjnh.infrastructure.datasource.kernel.dialect;

/*                OracleDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  oracle pagination dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.kernel.SqlDialectPort011;

import org.springframework.stereotype.Component;

/**
 * Oracle pagination dialect built on the double nested rownum form. The 12c
 * OFFSET / FETCH NEXT syntax is rejected by 11g, so the rownum form is used for
 * every Oracle version (11g through 21c). The two rownum aliases must not be
 * named {@code rn} in the inner query, otherwise the developer SQL that already
 * exposes a column named rn would collide.
 */

@Component
public class OracleDialect011 implements SqlDialectPort011 {

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.ORACLE;
    }

    /** {@inheritDoc} */
    @Override
    public String pageSql(String sql, long offset, int pageSize) {
        long lowerBound = offset + 1;
        long upperBound = offset + pageSize;

        return "SELECT * FROM ( SELECT klsjnh_inner.*, ROWNUM klsjnh_rn FROM ( " + sql
                + " ) klsjnh_inner WHERE ROWNUM <= " + upperBound
                + " ) WHERE klsjnh_rn >= " + lowerBound;
    }
}

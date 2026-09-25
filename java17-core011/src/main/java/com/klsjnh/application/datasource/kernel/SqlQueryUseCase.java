package com.klsjnh.application.datasource.kernel;

/*                SqlQueryUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  parameterized read-only sql query use case
 *
 */

import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.SqlGuard011;

import com.klsjnh.domain.datasource.kernel.SqlRoutingPort;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Data source center — read (pagination) use case: enforce the read-only guard,
 * then run a parameterized paged query against a dynamic datasource. The SQL
 * runs on a dynamic datasource, so no primary-database transaction is opened.
 */

@Service
public class SqlQueryUseCase {

    /**
     * SQL routing port.
     */
    private final SqlRoutingPort sqlRoutingPort;

    /**
     * Create the use case.
     *
     * @param sqlRoutingPort sql routing port
     */
    public SqlQueryUseCase(SqlRoutingPort sqlRoutingPort) {
        this.sqlRoutingPort = sqlRoutingPort;
    }

    /**
     * Run a read-only paged query on a dynamic datasource.
     *
     * @param dsCode    datasource code
     * @param sql       select statement with {@code ?} placeholders
     * @param params    bound parameters, nullable
     * @param pageIndex page index starting at 1
     * @param pageSize  page size, clamped to [10, 500]
     * @return page result
     */
    public PageResult011<Map<String, Object>> selectByPage(String dsCode, String sql, List<Object> params,
            Integer pageIndex, Integer pageSize) {
        SqlGuard011.assertReadOnly(sql);

        return sqlRoutingPort.selectListByPage(dsCode, sql, params, pageIndex, pageSize);
    }
}

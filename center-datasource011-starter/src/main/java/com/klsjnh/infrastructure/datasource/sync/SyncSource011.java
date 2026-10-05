package com.klsjnh.infrastructure.datasource.sync;

/*                SyncSource011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync source (reuse datasource paged query)
 *      2026.10.05  source guard: identifier whitelist (table) + read-only sql guard
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.SqlGuard011;

import com.klsjnh.domain.datasource.kernel.SqlRoutingPort;
import com.klsjnh.domain.datasource.sync.Endpoint;
import com.klsjnh.domain.datasource.sync.SyncSourcePort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Sync source implementation: reads one page from an endpoint through the
 * datasource center's parameterized paged query, so pagination and dialect
 * handling are shared with the read capability.
 */

@Component
public class SyncSource011 implements SyncSourcePort {

    /**
     * Identifier pattern (same whitelist as the sink side) — the source SQL is
     * executed on any dialect, so the identifier is never quoted.
     */
    private static final String IDENTIFIER = "[A-Za-z0-9_]+";

    /**
     * SQL routing port.
     */
    private final SqlRoutingPort sqlRoutingPort;

    /**
     * Create the source.
     *
     * @param sqlRoutingPort sql routing port
     */
    public SyncSource011(SqlRoutingPort sqlRoutingPort) {
        this.sqlRoutingPort = sqlRoutingPort;
    }

    /** {@inheritDoc} */
    @Override
    public List<Map<String, Object>> readPage(Endpoint endpoint, int pageSize, int pageIndex) {
        String sql = buildSourceSql(endpoint);

        PageResult011<Map<String, Object>> page = sqlRoutingPort.selectListByPage(endpoint.dsCode(), sql, null,
                pageIndex, pageSize);

        return page.rows();
    }

    /**
     * Build the source statement: a sql endpoint must pass the read-only
     * guard, a table endpoint is a whitelisted identifier concatenated into a
     * plain SELECT (no quoting — the source may be any dialect).
     *
     * @param endpoint source endpoint
     * @return read-only select statement
     */
    private String buildSourceSql(Endpoint endpoint) {
        if (endpoint.isSql()) {
            SqlGuard011.assertReadOnly(endpoint.data());
            return endpoint.data();
        }

        if (endpoint.data() == null || !endpoint.data().matches(IDENTIFIER)) {
            throw BusinessException.badRequest("illegal sql identifier: " + endpoint.data());
        }

        return "SELECT * FROM " + endpoint.data();
    }
}

package com.klsjnh.infrastructure.datasource.sync;

/*                SyncSource011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync source (reuse datasource paged query)
 *
 */

import com.klsjnh.common.page.PageResult011;

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
        String sql = endpoint.isSql() ? endpoint.data() : "SELECT * FROM " + endpoint.data();

        PageResult011<Map<String, Object>> page = sqlRoutingPort.selectListByPage(endpoint.dsCode(), sql, null,
                pageIndex, pageSize);

        return page.rows();
    }
}

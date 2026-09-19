package com.klsjnh.infrastructure.datasource.kernel;

/*                DataSourceProbe011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  data source probe 011 class
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;
import com.klsjnh.domain.datasource.kernel.DataSourceProbePort;

import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Connectivity probe for datasource candidates: builds a throwaway single
 * connection pool and opens exactly one connection.
 * <p>
 * The probe NEVER touches the registry — a successful test does not declare
 * anything and does not cache a pool. The pool is always closed in a finally
 * block. The result carries a client-safe message; the raw JDBC error stays in
 * the log only. A probe that cannot connect is a SUCCESSFUL probe action:
 * {@code success=false}, not an exception.
 * </p>
 */

@Slf4j
@Component
public class DataSourceProbe011 implements DataSourceProbePort {

    /**
     * Pool builder (probe profile: initialSize 0 / maxActive 1 / 10s wait).
     */
    private final DataSourcePools011 pools;

    /**
     * Create the probe.
     *
     * @param pools pool builder
     */
    public DataSourceProbe011(DataSourcePools011 pools) {
        this.pools = pools;
    }

    /**
     * Probe a candidate connection config.
     *
     * @param info connection info
     * @return probe result, never null
     */
    @Override
    public ProbeResult probe(ConnectionInfo info) {
        String funcName = "probe";

        DruidDataSource pool = null;

        try {
            pool = pools.buildForProbe(info);

            try (Connection connection = pool.getConnection()) {
                DatabaseMetaData meta = connection.getMetaData();
                String product = meta == null ? null : meta.getDatabaseProductName();
                String version = meta == null ? null : meta.getDatabaseProductVersion();

                log.info("{} {} ok product {} version {} ...", funcName, info.dsCode(), product, version);

                return ProbeResult.ok(product, version);
            }
        } catch (Exception ex) {
            log.warn("{} {} failed {} ...", funcName, info.dsCode(), ex.getMessage());

            return ProbeResult.fail(safeMessage(ex));
        } finally {
            closeQuietly(pool, info.dsCode(), funcName);
        }
    }

    /**
     * Close the throwaway pool, logging but never rethrowing a failure —
     * a probe outcome must not be masked by a close error.
     *
     * @param pool     probe pool, nullable
     * @param dsCode   datasource code
     * @param funcName operation name for the log
     */
    private void closeQuietly(DruidDataSource pool, String dsCode, String funcName) {
        if (pool == null) {
            return;
        }

        try {
            pool.close();
        } catch (Exception ex) {
            log.warn("{} {} close probe pool failed {} ...", funcName, dsCode, ex.getMessage());
        }
    }

    /**
     * Reduce a raw exception into a client-safe one line hint: the exception
     * message with the newlines collapsed. A JDBC error may echo the URL, never
     * the password.
     *
     * @param ex raw exception
     * @return one line hint, never blank
     */
    private String safeMessage(Exception ex) {
        String message = ex.getMessage();

        if (StringUtil011.isBlank(message)) {
            return ex.getClass().getSimpleName();
        }

        return message.replaceAll("\\s+", " ").trim();
    }
}

package com.klsjnh.infrastructure.dataservice011.support;

/*                BusinessModelingProbe011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  business modeling probe 011 class
 *
 */

import com.klsjnh.domain.dataservice011.BusinessModelingProbePort;
import com.klsjnh.domain.dataservice011.ProbeOutcome;
import com.klsjnh.domain.datasource.ConnectionInfo;

import com.klsjnh.infrastructure.datasource.DynamicDataSource011;
import com.klsjnh.infrastructure.datasource.DynamicDataSourceRegistryImpl;
import com.klsjnh.infrastructure.datasource.SqlDialect011;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Probe implementation: runs the read-only statement against the dynamic
 * datasource, reads {@code ResultSetMetaData} for type inference and samples a
 * few rows. An unreachable target or a broken statement is reported as a
 * failed {@link ProbeOutcome}, never thrown.
 */

@Component
public class BusinessModelingProbe011 implements BusinessModelingProbePort {

    /**
     * Paging row-number alias injected by the Oracle page wrapper — never a
     * business field, so it is filtered out of the inferred columns.
     */
    private static final String PAGING_ROW_NUMBER = "klsjnh_rn";

    /**
     * Registry (config declaration + lazy pool ensure).
     */
    private final DynamicDataSourceRegistryImpl registry;

    /**
     * Dialect strategy (dialect paging for the sample window).
     */
    private final SqlDialect011 dialect;

    /**
     * Routing datasource with the primary datasource as default target.
     */
    private final DataSource routingDataSource;

    /**
     * Create the probe.
     *
     * @param primaryDataSource the auto-configured primary datasource
     * @param registry          dynamic datasource registry
     * @param dialect           pagination dialect
     */
    public BusinessModelingProbe011(DataSource primaryDataSource, DynamicDataSourceRegistryImpl registry,
            SqlDialect011 dialect) {
        this.registry = registry;
        this.dialect = dialect;
        this.routingDataSource = new DynamicDataSource011(primaryDataSource);
    }

    /**
     * Probe one read-only statement.
     *
     * @param dsCode datasource code to run against, required
     * @param sql    read-only statement, required
     * @return probe outcome, never null
     */
    @Override
    public ProbeOutcome probe(String dsCode, String sql) {
        try {
            registry.ensurePool(dsCode);

            ConnectionInfo info = registry.getConfig(dsCode);
            String limited = dialect.pageSql(info.dsType(), sql, 0, SAMPLE_ROWS);

            DynamicDataSource011.set(dsCode);

            try {
                return read(limited);
            } finally {
                DynamicDataSource011.clear();
            }
        } catch (Exception ex) {
            return ProbeOutcome.fail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Execute the limited statement and read the column metadata plus a row
     * sample.
     *
     * @param limited limited statement
     * @return probe outcome
     */
    private ProbeOutcome read(String limited) throws Exception {
        try (Connection connection = routingDataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(limited);
                ResultSet resultSet = statement.executeQuery()) {

            ResultSetMetaData meta = resultSet.getMetaData();
            int count = meta.getColumnCount();
            List<ProbeOutcome.ProbeColumn> columns = new ArrayList<>();
            List<Integer> indexes = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                String label = meta.getColumnLabel(i);

                if (PAGING_ROW_NUMBER.equalsIgnoreCase(label)) {
                    continue;
                }

                columns.add(new ProbeOutcome.ProbeColumn(label, meta.getColumnType(i), meta.getColumnDisplaySize(i),
                        meta.isNullable(i) != ResultSetMetaData.columnNoNulls));
                indexes.add(i);
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            while (rows.size() < SAMPLE_ROWS && resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();

                for (int i = 0; i < columns.size(); i++) {
                    row.put(columns.get(i).code(), resultSet.getObject(indexes.get(i)));
                }

                rows.add(row);
            }

            return ProbeOutcome.ok(columns, rows);
        }
    }
}

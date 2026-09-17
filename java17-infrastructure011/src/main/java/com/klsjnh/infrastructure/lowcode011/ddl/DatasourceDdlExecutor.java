package com.klsjnh.infrastructure.lowcode011.ddl;

/*                DatasourceDdlExecutor class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  datasource ddl executor class
 *
 */

import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DDL executor against the primary datasource. Execution is gated by
 * {@code krt.lowcode.ddl-execute.enabled} (default false) so the feature can
 * ship dark and be switched on deliberately.
 */

@Component
public class DatasourceDdlExecutor implements MetadataDdlExecutorPort {

    /**
     * Primary datasource jdbc template.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Whether DDL execution is allowed.
     */
    private final boolean enabled;

    /**
     * Create the executor.
     *
     * @param dataSource primary datasource
     * @param enabled    ddl execution switch
     */
    public DatasourceDdlExecutor(DataSource dataSource,
            @Value("${krt.lowcode.ddl-execute.enabled:false}") boolean enabled) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.enabled = enabled;
    }

    /**
     * Whether the physical table exists in the current schema.
     *
     * @param physicalTable physical table name
     * @return true when present
     */
    @Override
    public boolean tableExists(String physicalTable) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class, physicalTable);

        return count != null && count > 0;
    }

    /**
     * Existing column names of a physical table (lower case).
     *
     * @param physicalTable physical table name
     * @return column names
     */
    @Override
    public Set<String> columnsOf(String physicalTable) {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT LOWER(column_name) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = ?",
                String.class, physicalTable);

        return new HashSet<>(columns);
    }

    /**
     * Execute one DDL statement; blocked when the switch is off.
     *
     * @param ddl ddl statement
     */
    @Override
    public void execute(String ddl) {
        if (!enabled) {
            throw new IllegalStateException("ddl execute disabled (krt.lowcode.ddl-execute.enabled=false)");
        }

        jdbcTemplate.execute(ddl);
    }
}

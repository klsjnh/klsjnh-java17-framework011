package com.klsjnh.infrastructure.lowcode011.sync;

/*                JdbcTemplateMetadataDataWriter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  jdbc template metadata data writer class
 *
 */

import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.domain.lowcode011.MetadataDataWriterPort;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Data writer against the primary datasource. Source column labels are matched
 * to target columns case-insensitively; platform base columns absent from the
 * source (id / status / create_time / update_time / dr) are filled with
 * defaults so a generated table's NOT NULL columns never block an import.
 */

@Component
public class JdbcTemplateMetadataDataWriter implements MetadataDataWriterPort {

    /**
     * Accepted identifier shape.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,59}$");

    /**
     * Primary datasource jdbc template.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * DDL executor (column catalog reads).
     */
    private final MetadataDdlExecutorPort ddlExecutor;

    /**
     * Create the writer.
     *
     * @param dataSource  primary datasource
     * @param ddlExecutor DDL executor
     */
    public JdbcTemplateMetadataDataWriter(DataSource dataSource, MetadataDdlExecutorPort ddlExecutor) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.ddlExecutor = ddlExecutor;
    }

    /**
     * Upsert rows into the physical table.
     *
     * @param physicalTable target table
     * @param rows          source rows
     * @return number of rows processed
     */
    @Override
    public int upsert(String physicalTable, String businessField, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String table = requireIdentifier(physicalTable);
        Set<String> existing = ddlExecutor.columnsOf(table);
        Map<String, String> mapping = resolveColumns(rows.get(0), existing);

        if (mapping.isEmpty()) {
            return 0;
        }

        List<String> targetColumns = new ArrayList<>();
        List<String> sourceKeys = new ArrayList<>();
        String businessSourceKey = null;

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            targetColumns.add(entry.getValue());
            sourceKeys.add(entry.getKey());

            if (businessField != null && entry.getValue().equalsIgnoreCase(businessField)) {
                businessSourceKey = entry.getKey();
            }
        }

        if (businessField != null && !businessField.isBlank() && businessSourceKey == null) {
            throw new IllegalArgumentException("business field not present in source: " + businessField);
        }

        String columnList = targetColumns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(","));
        String placeholders = targetColumns.stream().map(c -> "?").collect(Collectors.joining(","));
        String updates = targetColumns.stream()
                .filter(c -> !"id".equalsIgnoreCase(c))
                .map(c -> "`" + c + "`=VALUES(`" + c + "`)")
                .collect(Collectors.joining(","));

        String sql = "INSERT INTO `" + table + "` (" + columnList + ") VALUES (" + placeholders + ")"
                + (updates.isEmpty() ? "" : " ON DUPLICATE KEY UPDATE " + updates);

        List<Object[]> batchArgs = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Object[] values = new Object[sourceKeys.size()];

            for (int i = 0; i < sourceKeys.size(); i++) {
                String key = sourceKeys.get(i);
                values[i] = key.startsWith("#default:")
                        ? defaultValue(key.substring("#default:".length()))
                        : row.get(key);
            }

            batchArgs.add(values);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);

        return rows.size();
    }

    /**
     * Match source labels to target columns case-insensitively and add defaults
     * for the missing platform base columns.
     *
     * @param sampleRow first source row
     * @param existing  target columns (lower case)
     * @return source key → target column, insertion ordered
     */
    private Map<String, String> resolveColumns(Map<String, Object> sampleRow, Set<String> existing) {
        Map<String, String> mapping = new LinkedHashMap<>();

        for (String key : sampleRow.keySet()) {
            if (key != null && IDENTIFIER.matcher(key).matches() && existing.contains(key.toLowerCase(Locale.ROOT))) {
                mapping.put(key, key);
            }
        }

        addDefault(mapping, existing, "id");
        addDefault(mapping, existing, "status");
        addDefault(mapping, existing, "create_time");
        addDefault(mapping, existing, "update_time");
        addDefault(mapping, existing, "dr");

        return mapping;
    }

    /**
     * Register a default-bound base column when it exists and is not mapped.
     *
     * @param mapping  mapping
     * @param existing target columns
     * @param column   base column
     */
    private void addDefault(Map<String, String> mapping, Set<String> existing, String column) {
        if (existing.contains(column) && mapping.values().stream().noneMatch(c -> c.equalsIgnoreCase(column))) {
            mapping.put("#default:" + column, column);
        }
    }

    /**
     * Default value for a platform base column.
     *
     * @param column base column
     * @return default value
     */
    private Object defaultValue(String column) {
        switch (column) {
            case "id":
                return UUID.randomUUID().toString().replace("-", "");
            case "status":
                return "1";
            case "dr":
                return "0";
            case "create_time":
            case "update_time":
                return Timestamp.valueOf(DateUtil011.now());
            default:
                return null;
        }
    }

    /**
     * Validate a table identifier.
     *
     * @param value raw identifier
     * @return trimmed identifier
     */
    private String requireIdentifier(String value) {
        if (value == null || !IDENTIFIER.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("invalid identifier: " + value);
        }

        return value.trim();
    }
}

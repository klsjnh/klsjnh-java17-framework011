package com.klsjnh.infrastructure.lowcode011.runtime;

/*                JdbcTemplateMetadataDataAccess class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  jdbc template metadata data access class
 *
 */

import com.klsjnh.domain.lowcode011.MetadataDataAccessPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * JDBC data access for generated physical tables. Every identifier is validated
 * against a strict shape; every value is bound.
 */

@Component
public class JdbcTemplateMetadataDataAccess implements MetadataDataAccessPort {

    /**
     * Accepted identifier shape.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,59}$");

    /**
     * Primary datasource jdbc template.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Create the access.
     *
     * @param dataSource primary datasource
     */
    public JdbcTemplateMetadataDataAccess(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Page rows with equality filters.
     *
     * @param table   physical table
     * @param columns selected columns
     * @param filters equality filters
     * @param orderBy order column, nullable
     * @param offset  zero based offset
     * @param limit   page size
     * @return rows
     */
    @Override
    public List<Map<String, Object>> select(String table, List<String> columns, Map<String, Object> filters,
            String orderBy, int offset, int limit) {
        String cols = columns.stream().map(this::identifier).map(c -> "`" + c + "`")
                .collect(Collectors.joining(","));
        Where where = where(filters);
        String order = orderBy == null || orderBy.isBlank() ? "" : " ORDER BY `" + identifier(orderBy) + "`";
        String sql = "SELECT " + cols + " FROM `" + identifier(table) + "`" + where.sql + order + " LIMIT ? OFFSET ?";

        List<Object> args = new ArrayList<>(where.args);
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.queryForList(sql, args.toArray());
    }

    /**
     * Count rows with equality filters.
     *
     * @param table   physical table
     * @param filters equality filters
     * @return row count
     */
    @Override
    public long count(String table, Map<String, Object> filters) {
        Where where = where(filters);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + identifier(table) + "`" + where.sql,
                Long.class, where.args.toArray());

        return count == null ? 0 : count;
    }

    /**
     * Insert one row.
     *
     * @param table  physical table
     * @param values column → value
     * @return affected rows
     */
    @Override
    public int insert(String table, Map<String, Object> values) {
        String cols = values.keySet().stream().map(this::identifier).map(c -> "`" + c + "`")
                .collect(Collectors.joining(","));
        String marks = values.keySet().stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "INSERT INTO `" + identifier(table) + "` (" + cols + ") VALUES (" + marks + ")";

        return jdbcTemplate.update(sql, values.values().toArray());
    }

    /**
     * Update one row by key.
     *
     * @param table     physical table
     * @param keyColumn key column
     * @param keyValue  key value
     * @param values    column → value
     * @return affected rows
     */
    @Override
    public int updateByKey(String table, String keyColumn, Object keyValue, Map<String, Object> values) {
        if (values.isEmpty()) {
            return 0;
        }

        String sets = values.keySet().stream().map(this::identifier).map(c -> "`" + c + "`=?")
                .collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>(values.values());
        args.add(keyValue);
        String sql = "UPDATE `" + identifier(table) + "` SET " + sets + " WHERE `" + identifier(keyColumn) + "`=?";

        return jdbcTemplate.update(sql, args.toArray());
    }

    /**
     * Delete one row by key (logic delete when requested).
     *
     * @param table     physical table
     * @param keyColumn key column
     * @param keyValue  key value
     * @param logic     whether to logic delete
     * @return affected rows
     */
    @Override
    public int deleteByKey(String table, String keyColumn, Object keyValue, boolean logic) {
        String sql = logic
                ? "UPDATE `" + identifier(table) + "` SET `dr`='1' WHERE `" + identifier(keyColumn) + "`=?"
                : "DELETE FROM `" + identifier(table) + "` WHERE `" + identifier(keyColumn) + "`=?";

        return jdbcTemplate.update(sql, keyValue);
    }

    /**
     * Build the WHERE clause from equality filters.
     *
     * @param filters equality filters
     * @return where clause + args
     */
    private Where where(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new Where("", List.of());
        }

        List<String> clauses = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            clauses.add("`" + identifier(entry.getKey()) + "`=?");
            args.add(entry.getValue());
        }

        return new Where(" WHERE " + String.join(" AND ", clauses), args);
    }

    /**
     * Validate an identifier.
     *
     * @param value raw identifier
     * @return trimmed identifier
     */
    private String identifier(String value) {
        if (value == null || !IDENTIFIER.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("invalid identifier: " + value);
        }

        return value.trim();
    }

    /**
     * Where clause holder.
     *
     * @param sql  sql fragment
     * @param args bound args
     */
    private record Where(String sql, List<Object> args) {
    }
}

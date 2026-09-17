package com.klsjnh.application.lowcode011;

/*                OpenApiUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api public use case class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApi;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApiRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.lowcode011.MetadataDataAccessPort;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Public open API use case: api_key authenticated CRUD plus the metadata read
 * over a published object's physical table. Columns and table names come from
 * the live catalog (whitelist), values are bound, and every call is checked
 * against the object's auth mode and allowed operations.
 */

@Service
public class OpenApiUseCase {

    /**
     * Configuration repository.
     */
    private final JulyMetadataOpenApiRepository configRepository;

    /**
     * Snapshot repository (physical table).
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * Catalog reads (columns / table existence).
     */
    private final MetadataDdlExecutorPort ddlExecutor;

    /**
     * Generic data access.
     */
    private final MetadataDataAccessPort dataAccess;

    /**
     * Designer use case (metadata read).
     */
    private final JulyMetadataDesignerUseCase designerUseCase;

    /**
     * User audit port (dynamic objectCode for open API writes).
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param configRepository  configuration repository
     * @param versionRepository snapshot repository
     * @param ddlExecutor       catalog reads
     * @param dataAccess        generic data access
     * @param designerUseCase   designer use case
     * @param userAuditPort     user audit port
     */
    public OpenApiUseCase(JulyMetadataOpenApiRepository configRepository,
            JulyMetadataVersionRepository versionRepository, MetadataDdlExecutorPort ddlExecutor,
            MetadataDataAccessPort dataAccess, JulyMetadataDesignerUseCase designerUseCase,
            UserAuditPort userAuditPort) {
        this.configRepository = configRepository;
        this.versionRepository = versionRepository;
        this.ddlExecutor = ddlExecutor;
        this.dataAccess = dataAccess;
        this.designerUseCase = designerUseCase;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Read the object metadata (MetaDTO).
     *
     * @param objectName object name
     * @param apiKey     api key, nullable for authMode none
     * @return MetaDTO
     */
    public Map<String, Object> meta(String objectName, String apiKey) {
        authorize(objectName, apiKey, "query");

        return designerUseCase.load(objectName);
    }

    /**
     * Page rows of a published object.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       query body (filters, pageIndex, pageSize)
     * @return page result
     */
    public Map<String, Object> query(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "query");

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> filters = filter(body.get("filters"), columns);

        if (columns.contains("dr") && !filters.containsKey("dr")) {
            filters.put("dr", "0");
        }

        int pageIndex = integer(body.get("pageIndex"), 1);
        int pageSize = integer(body.get("pageSize"), 20);
        List<String> selected = new ArrayList<>(columns);
        long total = dataAccess.count(table, filters);
        List<Map<String, Object>> rows = dataAccess.select(table, selected, filters, "id", (pageIndex - 1) * pageSize,
                pageSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageIndex", pageIndex);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("rows", rows);

        return result;
    }

    /**
     * Insert one row.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       row values
     * @return affected rows
     */
    public int create(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "insert");

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> values = filter(body, columns);

        if (values.isEmpty()) {
            throw BusinessException.badRequest("no valid column in body");
        }

        fillBaseDefaults(values, columns);

        int rows = dataAccess.insert(table, values);
        audit(objectName, AuditType011.INSERT);

        return rows;
    }

    /**
     * Fill platform base columns that are NOT NULL on the generated table but
     * absent from the request (id / status / dr / create_time / update_time).
     *
     * @param values  column values (mutated)
     * @param columns target columns
     */
    private void fillBaseDefaults(Map<String, Object> values, Set<String> columns) {
        if (columns.contains("id") && !values.containsKey("id")) {
            values.put("id", UUID.randomUUID().toString().replace("-", ""));
        }

        if (columns.contains("status") && !values.containsKey("status")) {
            values.put("status", "1");
        }

        if (columns.contains("dr") && !values.containsKey("dr")) {
            values.put("dr", "0");
        }

        java.sql.Timestamp now = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());

        if (columns.contains("create_time") && !values.containsKey("create_time")) {
            values.put("create_time", now);
        }

        if (columns.contains("update_time") && !values.containsKey("update_time")) {
            values.put("update_time", now);
        }
    }

    /**
     * Update one row by id (or the given business key).
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       key + values
     * @return affected rows
     */
    public int update(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "update");

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = keyColumn(body, columns);
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        Map<String, Object> values = filter(body, columns);
        values.remove(keyColumn);

        int rows = dataAccess.updateByKey(table, keyColumn, keyValue, values);
        audit(objectName, AuditType011.UPDATE);

        return rows;
    }

    /**
     * Delete one row by id (or the given business key); logic delete when the
     * table carries a {@code dr}.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       key
     * @return affected rows
     */
    public int delete(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "delete");

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = keyColumn(body, columns);
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        int rows = dataAccess.deleteByKey(table, keyColumn, keyValue, columns.contains("dr"));
        audit(objectName, AuditType011.DELETE);

        return rows;
    }

    /**
     * Record an audit row with the dynamic object code; never breaks the write.
     *
     * @param objectName object name
     * @param type       audit type
     */
    private void audit(String objectName, AuditType011 type) {
        try {
            userAuditPort.record(null, "open-api", type, objectName,
                    "open-api " + type.name().toLowerCase(Locale.ROOT) + " " + objectName, null);
        } catch (Exception ignored) {
            // audit must never break the business write
        }
    }

    /**
     * Resolve the physical table of a published object.
     *
     * @param objectName object name
     * @return physical table
     */
    private String physicalTable(String objectName) {
        JulyMetadataVersion latest = versionRepository.findLatest(objectName);

        if (latest == null) {
            throw BusinessException.badRequest("object not published: " + objectName);
        }

        return latest.physicalTable();
    }

    /**
     * Authorize an open API call: enabled + auth mode + allowed operation.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param op         operation (query / insert / update / delete)
     */
    private void authorize(String objectName, String apiKey, String op) {
        JulyMetadataOpenApi config = configRepository.findByObjectName(objectName);

        if (config == null || !config.enabled()) {
            throw BusinessException.unauthorized("open api not enabled: " + objectName);
        }

        if ("apiKey".equals(config.authMode()) && !apiKey.equals(config.apiKey())) {
            throw BusinessException.unauthorized("invalid apiKey");
        }

        if (!allowOps(config.allowedOps()).contains(op)) {
            throw BusinessException.unauthorized("operation not allowed: " + op);
        }
    }

    /**
     * Parse the allowed operations.
     *
     * @param ops comma list
     * @return operations
     */
    private List<String> allowOps(String ops) {
        return ops == null || ops.isBlank() ? List.of() : List.of(ops.split(","));
    }

    /**
     * Choose the update/delete key column.
     *
     * @param body    request body
     * @param columns target columns
     * @return key column
     */
    private String keyColumn(Map<String, Object> body, Set<String> columns) {
        if (body.containsKey("sid")) {
            return "sid";
        }

        return "id";
    }

    /**
     * Keep only keys that are real target columns.
     *
     * @param raw     raw map
     * @param columns target columns
     * @return filtered map
     */
    private Map<String, Object> filter(Object raw, Set<String> columns) {
        Map<String, Object> filtered = new LinkedHashMap<>();

        if (raw instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) raw).entrySet()) {
                String key = String.valueOf(entry.getKey()).toLowerCase(java.util.Locale.ROOT);

                if (columns.contains(key)) {
                    filtered.put(key, entry.getValue());
                }
            }
        }

        return filtered;
    }

    /**
     * Read a positive int with a default.
     *
     * @param value    raw value
     * @param fallback default
     * @return int
     */
    private int integer(Object value, int fallback) {
        int v = value instanceof Number ? ((Number) value).intValue() : fallback;

        return v < 1 ? fallback : v;
    }
}

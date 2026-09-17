package com.klsjnh.application.lowcode011;

/*                JulyMetadataRuntimeUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata runtime use case class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.lowcode011.MetadataDataAccessPort;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.system011.menu.JulyMenuRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runtime use case: publish a published object as a runtime menu, list runtime
 * menus, and serve the internal (JWT) dynamic CRUD of a published object. The
 * data plane reuses the generic access port (catalog whitelist + bound values).
 */

@Service
public class JulyMetadataRuntimeUseCase {

    /**
     * Runtime route prefix.
     */
    private static final String ROUTE_PREFIX = "/runtime/";

    /**
     * Metadata CRUD use case.
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository.
     */
    private final JulyMetadataRepository metadataRepository;

    /**
     * Snapshot repository (physical table / version).
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * Catalog reads (columns).
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
     * Menu repository (runtime menu entry).
     */
    private final JulyMenuRepository menuRepository;

    /**
     * Metadata-driven value validator.
     */
    private final MetadataValueValidator valueValidator;

    /**
     * User audit port (dynamic objectCode for runtime writes).
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param metadataUseCase    metadata CRUD use case
     * @param metadataRepository metadata repository
     * @param versionRepository  snapshot repository
     * @param ddlExecutor        catalog reads
     * @param dataAccess         generic data access
     * @param designerUseCase    designer use case
     * @param menuRepository     menu repository
     * @param valueValidator     value validator
     * @param userAuditPort      user audit port
     */
    public JulyMetadataRuntimeUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository metadataRepository,
            JulyMetadataVersionRepository versionRepository, MetadataDdlExecutorPort ddlExecutor,
            MetadataDataAccessPort dataAccess, JulyMetadataDesignerUseCase designerUseCase,
            JulyMenuRepository menuRepository, MetadataValueValidator valueValidator, UserAuditPort userAuditPort) {
        this.metadataUseCase = metadataUseCase;
        this.metadataRepository = metadataRepository;
        this.versionRepository = versionRepository;
        this.ddlExecutor = ddlExecutor;
        this.dataAccess = dataAccess;
        this.designerUseCase = designerUseCase;
        this.menuRepository = menuRepository;
        this.valueValidator = valueValidator;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Publish a published object as a runtime menu (idempotent: re-publish
     * updates the existing entry).
     *
     * @param objectName     object name
     * @param parentMenuCode parent menu code, nullable for root
     * @param projectCode    project code (reserved)
     * @return menu entry
     */
    public Map<String, Object> publishMenu(String objectName, String parentMenuCode, String projectCode) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);

        if (versionRepository.findLatest(objectName) == null) {
            throw BusinessException.badRequest("object not published: " + objectName);
        }

        String menuCode = "rt_" + objectName;
        String menuName = metadata.description() == null || metadata.description().isBlank()
                ? objectName
                : metadata.description();
        String route = ROUTE_PREFIX + objectName;
        String parentId = "";

        if (parentMenuCode != null && !parentMenuCode.isBlank()) {
            JulyMenu parent = menuRepository.findByCode(parentMenuCode);
            if (parent == null) {
                throw BusinessException.badRequest("parent menu not found: " + parentMenuCode);
            }
            parentId = parent.id().value();
        }

        JulyMenu existing = menuRepository.findByCode(menuCode);

        if (existing == null) {
            menuRepository.insert(JulyMenu.create(EntityId.generate(), menuCode, menuName, "2", null, route, null,
                    null, parentId, 9999, AuditInfo.empty()));
        } else {
            existing.updateBasics(menuName, "2", null, route, null, null, parentId, existing.sortOrder());
            menuRepository.update(existing);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", objectName);
        result.put("menuCode", menuCode);
        result.put("menuName", menuName);
        result.put("routerPath", route);
        result.put("parentMenuCode", parentMenuCode);

        return result;
    }

    /**
     * List runtime menus (menus whose route is under {@code /runtime/}).
     *
     * @return runtime menu rows
     */
    public List<Map<String, Object>> listRuntimeMenus() {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JulyMenu menu : menuRepository.findPage(0, 2000, null)) {
            String route = menu.menuRoute();

            if (route == null || !route.startsWith(ROUTE_PREFIX)) {
                continue;
            }

            String objectName = route.substring(ROUTE_PREFIX.length());
            JulyMetadataVersion latest = versionRepository.findLatest(objectName);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("objectName", objectName);
            row.put("menuCode", menu.menuCode());
            row.put("menuName", menu.menuName());
            row.put("routerPath", route);
            row.put("version", latest == null ? null : latest.version());
            row.put("publishStatus", latest == null ? "draft" : "published");
            rows.add(row);
        }

        return rows;
    }

    /**
     * Object metadata (MetaDTO) for the runtime form.
     *
     * @param objectName object name
     * @return MetaDTO
     */
    public Map<String, Object> meta(String objectName) {
        requirePublished(objectName);

        return designerUseCase.load(objectName);
    }

    /**
     * Page rows of a published object.
     *
     * @param objectName object name
     * @param body       query body (filters, pageIndex, pageSize)
     * @return page result
     */
    public Map<String, Object> query(String objectName, Map<String, Object> body) {
        requirePublished(objectName);

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> filters = filter(body.get("filters"), columns);

        if (columns.contains("dr") && !filters.containsKey("dr")) {
            filters.put("dr", "0");
        }

        int pageIndex = integer(body.get("pageIndex"), 1);
        int pageSize = integer(body.get("pageSize"), 20);
        long total = dataAccess.count(table, filters);
        List<Map<String, Object>> rows = dataAccess.select(table, new ArrayList<>(columns), filters, "id",
                (pageIndex - 1) * pageSize, pageSize);

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
     * @param body       row values
     * @return affected rows
     */
    public int create(String objectName, Map<String, Object> body, Operator011 operator) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> values = filter(body, columns);

        if (values.isEmpty()) {
            throw BusinessException.badRequest("no valid column in body");
        }

        fillBaseDefaults(values, columns);
        ensureValid(values, metadata, false);

        int rows = dataAccess.insert(table, values);
        audit(operator, AuditType011.INSERT, objectName);

        return rows;
    }

    /**
     * Update one row by id (or {@code sid}).
     *
     * @param objectName object name
     * @param body       key + values
     * @return affected rows
     */
    public int update(String objectName, Map<String, Object> body, Operator011 operator) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = body.containsKey("sid") ? "sid" : "id";
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        Map<String, Object> values = filter(body, columns);
        values.remove(keyColumn);
        ensureValid(values, metadata, true);

        int rows = dataAccess.updateByKey(table, keyColumn, keyValue, values);
        audit(operator, AuditType011.UPDATE, objectName);

        return rows;
    }

    /**
     * Delete one row by id (or {@code sid}); logic delete when the table has a
     * {@code dr}.
     *
     * @param objectName object name
     * @param body       key
     * @return affected rows
     */
    public int delete(String objectName, Map<String, Object> body, Operator011 operator) {
        requirePublished(objectName);

        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = body.containsKey("sid") ? "sid" : "id";
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        int rows = dataAccess.deleteByKey(table, keyColumn, keyValue, columns.contains("dr"));
        audit(operator, AuditType011.DELETE, objectName);

        return rows;
    }

    /**
     * Record an audit row with the dynamic object code; never breaks the write.
     *
     * @param operator   current operator, nullable (debug)
     * @param type       audit type
     * @param objectName object name
     */
    private void audit(Operator011 operator, AuditType011 type, String objectName) {
        try {
            userAuditPort.record(operator == null ? null : operator.id(),
                    operator == null ? null : operator.userAccount(), type, objectName,
                    "runtime " + type.name().toLowerCase(Locale.ROOT) + " " + objectName,
                    operator == null ? null : operator.ip());
        } catch (Exception ignored) {
            // audit must never break the business write
        }
    }

    /**
     * Ensure the object is published.
     *
     * @param objectName object name
     */
    private void requirePublished(String objectName) {
        if (metadataRepository.findByObjectName(objectName) == null) {
            throw BusinessException.recordNotFound(objectName);
        }
    }

    /**
     * Validate a write payload against the object fields.
     *
     * @param values   values
     * @param metadata object metadata
     * @param partial  true for update
     */
    private void ensureValid(Map<String, Object> values, JulyMetadata metadata, boolean partial) {
        List<String> errors = valueValidator.validate(values, metadata.fields(), partial);

        if (!errors.isEmpty()) {
            throw BusinessException.badRequest("validation failed: " + String.join("; ", errors));
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
     * Fill platform base columns that are NOT NULL but absent from the request.
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
                String key = String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT);

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

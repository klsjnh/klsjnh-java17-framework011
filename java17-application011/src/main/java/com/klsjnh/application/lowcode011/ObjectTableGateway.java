package com.klsjnh.application.lowcode011;

/*                ObjectTableGateway class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  object table gateway class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.lowcode011.CurrentOperatorPort;
import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.lowcode011.MetadataDataAccessPort;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;
import com.klsjnh.domain.lowcode011.ObjectTablePolicy;
import com.klsjnh.domain.lowcode011.records.BaseColumn011;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The object-table kernel: physical-table resolution, catalog whitelisting, base
 * column defaults, logic delete, paging and metadata validation live here in
 * one place, so the runtime / open-api / data-sync use cases keep only their
 * orchestration differences.
 */

@Service
public class ObjectTableGateway {

    /**
     * Generic data access.
     */
    private final MetadataDataAccessPort dataAccess;

    /**
     * Catalog reads (columns).
     */
    private final MetadataDdlExecutorPort ddlExecutor;

    /**
     * Metadata-driven value validator.
     */
    private final MetadataValueValidator validator;

    /**
     * Snapshot repository (physical table).
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * Metadata repository (fields for validation).
     */
    private final JulyMetadataRepository metadataRepository;

    /**
     * Current operator port (audit column stamping).
     */
    private final CurrentOperatorPort currentOperatorPort;

    /**
     * Create the gateway.
     *
     * @param dataAccess          generic data access
     * @param ddlExecutor         catalog reads
     * @param validator           value validator
     * @param versionRepository   snapshot repository
     * @param metadataRepository  metadata repository
     * @param currentOperatorPort current operator port
     */
    public ObjectTableGateway(MetadataDataAccessPort dataAccess, MetadataDdlExecutorPort ddlExecutor,
            MetadataValueValidator validator, JulyMetadataVersionRepository versionRepository,
            JulyMetadataRepository metadataRepository, CurrentOperatorPort currentOperatorPort) {
        this.dataAccess = dataAccess;
        this.ddlExecutor = ddlExecutor;
        this.validator = validator;
        this.versionRepository = versionRepository;
        this.metadataRepository = metadataRepository;
        this.currentOperatorPort = currentOperatorPort;
    }

    /**
     * Resolve the physical table of a published object.
     *
     * @param objectName object name
     * @return physical table
     */
    public String physicalTable(String objectName) {
        JulyMetadataVersion latest = versionRepository.findLatest(objectName);

        if (latest == null) {
            throw BusinessException.badRequest("object not published: " + objectName);
        }

        return latest.physicalTable();
    }

    /**
     * Page rows with equality filters (logic-deleted rows excluded when
     * supported).
     *
     * @param objectName object name
     * @param filters    equality filters, nullable
     * @param pageIndex  page index, nullable
     * @param pageSize   page size, nullable (clamped [1,500])
     * @return page result map
     */
    public Map<String, Object> query(String objectName, Map<String, Object> filters, Integer pageIndex,
            Integer pageSize) {
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> where = ObjectTablePolicy.filter(filters, columns);
        ObjectTablePolicy.withDrFilter(where, columns);
        int[] page = ObjectTablePolicy.page(pageIndex, pageSize);
        long total = dataAccess.count(table, where);
        List<Map<String, Object>> rows = dataAccess.select(table, new ArrayList<>(columns), where, "id",
                (page[0] - 1) * page[1], page[1]);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageIndex", page[0]);
        result.put("pageSize", page[1]);
        result.put("total", total);
        result.put("rows", rows);

        return result;
    }

    /**
     * Insert one row (base columns filled, metadata validated).
     *
     * @param objectName object name
     * @param body       row values
     * @return affected rows
     */
    public int insert(String objectName, Map<String, Object> body) {
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        Map<String, Object> values = ObjectTablePolicy.filter(body, columns);

        if (values.isEmpty()) {
            throw BusinessException.badRequest("no valid column in body");
        }

        ObjectTablePolicy.fillBaseDefaults(values, columns);
        stampOperator(values, columns, true);
        validate(values, fields(objectName), false);

        return dataAccess.insert(table, values);
    }

    /**
     * Update one row by id / sid (update-time refreshed, metadata validated).
     *
     * @param objectName object name
     * @param body       key + values
     * @return affected rows
     */
    public int update(String objectName, Map<String, Object> body) {
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = ObjectTablePolicy.keyColumn(body);
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        Map<String, Object> values = ObjectTablePolicy.filter(body, columns);
        values.remove(keyColumn);
        ObjectTablePolicy.stampUpdate(values, columns);
        stampOperator(values, columns, false);
        validate(values, fields(objectName), true);

        return dataAccess.updateByKey(table, keyColumn, keyValue, values);
    }

    /**
     * Delete one row by id / sid (logic delete when supported).
     *
     * @param objectName object name
     * @param body       key
     * @return affected rows
     */
    public int delete(String objectName, Map<String, Object> body) {
        String table = physicalTable(objectName);
        Set<String> columns = ddlExecutor.columnsOf(table);
        String keyColumn = ObjectTablePolicy.keyColumn(body);
        Object keyValue = body.get(keyColumn);

        if (keyValue == null) {
            throw BusinessException.badRequest(keyColumn + " required");
        }

        return dataAccess.deleteByKey(table, keyColumn, keyValue, ObjectTablePolicy.logicDelete(columns));
    }

    /**
     * Stamp create_by / update_by from the current request operator.
     *
     * @param values  column values (mutated)
     * @param columns target columns
     * @param insert  true for insert (both create_by and update_by)
     */
    private void stampOperator(Map<String, Object> values, Set<String> columns, boolean insert) {
        Operator011 operator = currentOperatorPort.current();

        if (operator == null) {
            return;
        }

        if (insert && columns.contains(BaseColumn011.CREATE_BY)) {
            values.put(BaseColumn011.CREATE_BY, operator.id());
        }

        if (columns.contains(BaseColumn011.UPDATE_BY)) {
            values.put(BaseColumn011.UPDATE_BY, operator.id());
        }
    }

    /**
     * Load the object fields for validation.
     *
     * @param objectName object name
     * @return fields
     */
    private List<com.klsjnh.domain.lowcode011.JulyMetadataField> fields(String objectName) {
        JulyMetadata metadata = metadataRepository.findByObjectName(objectName);

        if (metadata == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        return metadata.fields();
    }

    /**
     * Validate a write payload against the object fields.
     *
     * @param values  values
     * @param fields  object fields
     * @param partial true for update
     */
    private void validate(Map<String, Object> values, List<com.klsjnh.domain.lowcode011.JulyMetadataField> fields,
            boolean partial) {
        List<String> errors = validator.validate(values, fields, partial);

        if (!errors.isEmpty()) {
            throw BusinessException.badRequest("validation failed: " + String.join("; ", errors));
        }
    }
}

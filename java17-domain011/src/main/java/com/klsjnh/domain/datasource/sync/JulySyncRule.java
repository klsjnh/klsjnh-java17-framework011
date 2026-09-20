package com.klsjnh.domain.datasource.sync;

/*                JulySyncRule class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule aggregate (master)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * Sync rule aggregate root (data source center): one rule describing "from where
 * to where, how" — a source endpoint, a target endpoint, the business key for
 * idempotent upsert and the conflict strategy. The column mapping lives in the
 * {@link JulySyncRuleColumn} child aggregate.
 */

public class JulySyncRule {

    /**
     * Default sort order.
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Default page size.
     */
    private static final int DEFAULT_PAGE_SIZE = 500;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Sync code, unique and immutable.
     */
    private final String syncCode;

    /**
     * Sync name.
     */
    private String syncName;

    /**
     * Source datasource code.
     */
    private String sourceDsCode;

    /**
     * Source kind (sql / table / object).
     */
    private String sourceKind;

    /**
     * Source data (sql text / table name / object name).
     */
    private String sourceData;

    /**
     * Target datasource code.
     */
    private String targetDsCode;

    /**
     * Target kind (table / object).
     */
    private String targetKind;

    /**
     * Target data (table name / object name).
     */
    private String targetData;

    /**
     * Mode (full / incr).
     */
    private String mode;

    /**
     * Business key columns (target side, CSV).
     */
    private String syncKey;

    /**
     * Conflict strategy (upsert / append).
     */
    private String conflict;

    /**
     * JSON extension slot (reserved).
     */
    private String options;

    /**
     * Page size.
     */
    private Integer pageSize;

    /**
     * Manual sort order.
     */
    private Integer sortOrder;

    /**
     * Row status.
     */
    private String status;

    /**
     * Remark.
     */
    private String remark;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (rehydration path).
     *
     * @param id           primary key
     * @param syncCode     sync code
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceKind   source kind
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetKind   target kind
     * @param targetData   target data
     * @param mode         mode
     * @param syncKey      business key columns
     * @param conflict     conflict strategy
     * @param options      json extension slot
     * @param pageSize     page size
     * @param sortOrder    sort order
     * @param status       row status
     * @param remark       remark
     * @param audit        audit info
     */
    public JulySyncRule(EntityId id, String syncCode, String syncName, String sourceDsCode, String sourceKind,
            String sourceData, String targetDsCode, String targetKind, String targetData, String mode, String syncKey,
            String conflict, String options, Integer pageSize, Integer sortOrder, String status, String remark,
            AuditInfo audit) {
        validate(syncCode, syncName, sourceDsCode, sourceData, targetDsCode, targetData, syncKey);

        this.id = id;
        this.syncCode = syncCode;
        this.syncName = syncName;
        this.sourceDsCode = sourceDsCode;
        this.sourceKind = sourceKind == null ? Endpoint.KIND_SQL : sourceKind;
        this.sourceData = sourceData;
        this.targetDsCode = targetDsCode;
        this.targetKind = targetKind == null ? Endpoint.KIND_TABLE : targetKind;
        this.targetData = targetData;
        this.mode = mode == null ? "full" : mode;
        this.syncKey = syncKey;
        this.conflict = conflict == null ? "upsert" : conflict;
        this.options = options;
        this.pageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new rule.
     *
     * @param id           primary key
     * @param syncCode     sync code
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceKind   source kind
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetKind   target kind
     * @param targetData   target data
     * @param mode         mode
     * @param syncKey      business key columns
     * @param conflict     conflict strategy
     * @param pageSize     page size
     * @param remark       remark
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulySyncRule create(EntityId id, String syncCode, String syncName, String sourceDsCode,
            String sourceKind, String sourceData, String targetDsCode, String targetKind, String targetData,
            String mode, String syncKey, String conflict, Integer pageSize, String remark, AuditInfo audit) {
        return new JulySyncRule(id, syncCode, syncName, sourceDsCode, sourceKind, sourceData, targetDsCode, targetKind,
                targetData, mode, syncKey, conflict, null, pageSize, DEFAULT_SORT_ORDER, Status011.ENABLED.getCode(),
                remark, audit);
    }

    /**
     * Update the mutable fields (syncCode is immutable).
     *
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceKind   source kind
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetKind   target kind
     * @param targetData   target data
     * @param mode         mode
     * @param syncKey      business key columns
     * @param conflict     conflict strategy
     * @param pageSize     page size
     * @param remark       remark
     * @param status       row status, null keeps the stored one
     */
    public void update(String syncName, String sourceDsCode, String sourceKind, String sourceData, String targetDsCode,
            String targetKind, String targetData, String mode, String syncKey, String conflict, Integer pageSize,
            String remark, String status) {
        validate(this.syncCode, syncName, sourceDsCode, sourceData, targetDsCode, targetData, syncKey);

        this.syncName = syncName;
        this.sourceDsCode = sourceDsCode;
        this.sourceKind = sourceKind == null ? Endpoint.KIND_SQL : sourceKind;
        this.sourceData = sourceData;
        this.targetDsCode = targetDsCode;
        this.targetKind = targetKind == null ? Endpoint.KIND_TABLE : targetKind;
        this.targetData = targetData;
        this.mode = mode == null ? "full" : mode;
        this.syncKey = syncKey;
        this.conflict = conflict == null ? "upsert" : conflict;
        this.remark = remark;

        if (pageSize != null && pageSize > 0) {
            this.pageSize = pageSize;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * The source endpoint.
     *
     * @return source endpoint
     */
    public Endpoint sourceEndpoint() {
        return new Endpoint(sourceDsCode, sourceKind, sourceData);
    }

    /**
     * The target endpoint.
     *
     * @return target endpoint
     */
    public Endpoint targetEndpoint() {
        return new Endpoint(targetDsCode, targetKind, targetData);
    }

    /**
     * Validate the required fields.
     *
     * @param syncCode     sync code
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetData   target data
     * @param syncKey      business key columns
     */
    private static void validate(String syncCode, String syncName, String sourceDsCode, String sourceData,
            String targetDsCode, String targetData, String syncKey) {
        StringUtil011.requirePresent(syncCode, "sync code", 60);
        StringUtil011.requirePresent(syncName, "sync name", 100);
        StringUtil011.requirePresent(sourceDsCode, "source datasource code", 60);
        StringUtil011.requirePresent(sourceData, "source data", 2000);
        StringUtil011.requirePresent(targetDsCode, "target datasource code", 60);
        StringUtil011.requirePresent(targetData, "target data", 200);
        StringUtil011.requirePresent(syncKey, "sync key", 500);
    }

    /**
     * Get the primary key.
     *
     * @return id
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the sync code.
     *
     * @return sync code
     */
    public String syncCode() {
        return syncCode;
    }

    /**
     * Get the sync name.
     *
     * @return sync name
     */
    public String syncName() {
        return syncName;
    }

    /**
     * Get the source datasource code.
     *
     * @return source ds code
     */
    public String sourceDsCode() {
        return sourceDsCode;
    }

    /**
     * Get the source kind.
     *
     * @return source kind
     */
    public String sourceKind() {
        return sourceKind;
    }

    /**
     * Get the source data.
     *
     * @return source data
     */
    public String sourceData() {
        return sourceData;
    }

    /**
     * Get the target datasource code.
     *
     * @return target ds code
     */
    public String targetDsCode() {
        return targetDsCode;
    }

    /**
     * Get the target kind.
     *
     * @return target kind
     */
    public String targetKind() {
        return targetKind;
    }

    /**
     * Get the target data.
     *
     * @return target data
     */
    public String targetData() {
        return targetData;
    }

    /**
     * Get the mode.
     *
     * @return mode
     */
    public String mode() {
        return mode;
    }

    /**
     * Get the business key columns.
     *
     * @return sync key
     */
    public String syncKey() {
        return syncKey;
    }

    /**
     * Get the conflict strategy.
     *
     * @return conflict
     */
    public String conflict() {
        return conflict;
    }

    /**
     * Get the json extension slot.
     *
     * @return options
     */
    public String options() {
        return options;
    }

    /**
     * Get the page size.
     *
     * @return page size
     */
    public Integer pageSize() {
        return pageSize;
    }

    /**
     * Get the sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the row status.
     *
     * @return status
     */
    public String status() {
        return status;
    }

    /**
     * Get the remark.
     *
     * @return remark
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the audit info.
     *
     * @return audit
     */
    public AuditInfo audit() {
        return audit;
    }
}

package com.klsjnh.domain.datasource.sync;

/*                JulySyncRuleColumn class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule column aggregate (child)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * Sync rule column aggregate (child of {@link JulySyncRule}): one mapping row —
 * source column / source type → target column / target type, with an optional
 * value transform.
 */

public class JulySyncRuleColumn {

    /**
     * Default sort order.
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Master id (pk_mt).
     */
    private final String pkMt;

    /**
     * Source column.
     */
    private String sourceColumn;

    /**
     * Source neutral type.
     */
    private String sourceType;

    /**
     * Target column.
     */
    private String targetColumn;

    /**
     * Target neutral type.
     */
    private String targetType;

    /**
     * Optional value transform.
     */
    private String transform;

    /**
     * Sort order.
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
     * @param pkMt         master id
     * @param sourceColumn source column
     * @param sourceType   source neutral type
     * @param targetColumn target column
     * @param targetType   target neutral type
     * @param transform    value transform, nullable
     * @param sortOrder    sort order
     * @param status       row status
     * @param remark       remark
     * @param audit        audit info
     */
    public JulySyncRuleColumn(EntityId id, String pkMt, String sourceColumn, String sourceType, String targetColumn,
            String targetType, String transform, Integer sortOrder, String status, String remark, AuditInfo audit) {
        validate(sourceColumn, targetColumn);

        this.id = id;
        this.pkMt = pkMt;
        this.sourceColumn = sourceColumn;
        this.sourceType = sourceType == null ? "string" : sourceType;
        this.targetColumn = targetColumn;
        this.targetType = targetType == null ? "string" : targetType;
        this.transform = transform;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new column row.
     *
     * @param id           primary key
     * @param pkMt         master id
     * @param sourceColumn source column
     * @param sourceType   source neutral type
     * @param targetColumn target column
     * @param targetType   target neutral type
     * @param transform    value transform, nullable
     * @param sortOrder    sort order
     * @param remark       remark
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulySyncRuleColumn create(EntityId id, String pkMt, String sourceColumn, String sourceType,
            String targetColumn, String targetType, String transform, Integer sortOrder, String remark,
            AuditInfo audit) {
        return new JulySyncRuleColumn(id, pkMt, sourceColumn, sourceType, targetColumn, targetType, transform,
                sortOrder, Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * Update the mutable fields.
     *
     * @param sourceColumn source column
     * @param sourceType   source neutral type
     * @param targetColumn target column
     * @param targetType   target neutral type
     * @param transform    value transform
     * @param sortOrder    sort order
     * @param remark       remark
     * @param status       row status, null keeps the stored one
     */
    public void update(String sourceColumn, String sourceType, String targetColumn, String targetType, String transform,
            Integer sortOrder, String remark, String status) {
        validate(sourceColumn, targetColumn);

        this.sourceColumn = sourceColumn;
        this.sourceType = sourceType == null ? "string" : sourceType;
        this.targetColumn = targetColumn;
        this.targetType = targetType == null ? "string" : targetType;
        this.transform = transform;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the required fields.
     *
     * @param sourceColumn source column
     * @param targetColumn target column
     */
    private static void validate(String sourceColumn, String targetColumn) {
        StringUtil011.requirePresent(sourceColumn, "source column", 128);
        StringUtil011.requirePresent(targetColumn, "target column", 128);
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
     * Get the master id.
     *
     * @return pk_mt
     */
    public String pkMt() {
        return pkMt;
    }

    /**
     * Get the source column.
     *
     * @return source column
     */
    public String sourceColumn() {
        return sourceColumn;
    }

    /**
     * Get the source neutral type.
     *
     * @return source type
     */
    public String sourceType() {
        return sourceType;
    }

    /**
     * Get the target column.
     *
     * @return target column
     */
    public String targetColumn() {
        return targetColumn;
    }

    /**
     * Get the target neutral type.
     *
     * @return target type
     */
    public String targetType() {
        return targetType;
    }

    /**
     * Get the value transform.
     *
     * @return transform, nullable
     */
    public String transform() {
        return transform;
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

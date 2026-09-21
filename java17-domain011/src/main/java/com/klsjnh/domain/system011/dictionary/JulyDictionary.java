package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionary class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyDictionary aggregate root (system management context): one dictionary
 * type — code is the program-facing key (unique, immutable), the items live in
 * the child entity.
 */

public class JulyDictionary {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Dictionary code, unique, immutable.
     */
    private final String dictionaryCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Dictionary display name.
     */
    private String dictionaryName;

    /**
     * Row status: '1' enabled / '0' disabled (disabled is invisible to readers).
     */
    private String status;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id             primary key
     * @param dictionaryCode dictionary code, unique
     * @param sortOrder      manual sort order, null falls back to the default
     * @param dictionaryName dictionary display name
     * @param status         row status
     * @param remark         remark, optional
     * @param audit          audit info
     */
    public JulyDictionary(EntityId id, String dictionaryCode, Integer sortOrder, String dictionaryName, String status,
            String remark, AuditInfo audit) {
        this.id = id;
        this.dictionaryCode = dictionaryCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.dictionaryName = dictionaryName;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new dictionary type.
     *
     * @param id             primary key
     * @param dictionaryCode dictionary code, unique, max 60
     * @param sortOrder      manual sort order, null falls back to the default
     * @param dictionaryName dictionary display name, max 100
     * @param status         row status, null falls back to enabled
     * @param remark         remark, optional, max 300
     * @param audit          audit info
     * @return new aggregate
     */
    public static JulyDictionary create(EntityId id, String dictionaryCode, Integer sortOrder, String dictionaryName,
            String status, String remark, AuditInfo audit) {
        validate(dictionaryCode, dictionaryName, remark);

        return new JulyDictionary(id, dictionaryCode, sortOrder, dictionaryName,
                status == null ? Status011.ENABLED.getCode() : status, remark, audit);
    }

    /**
     * Update the mutable fields (dictionaryCode is immutable).
     *
     * @param dictionaryName dictionary display name
     * @param sortOrder      manual sort order, null keeps the stored one
     * @param status         row status, null keeps the stored one
     * @param remark         remark, optional
     */
    public void update(String dictionaryName, Integer sortOrder, String status, String remark) {
        validate(this.dictionaryCode, dictionaryName, remark);
        this.dictionaryName = dictionaryName;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the shared create / update basics.
     *
     * @param dictionaryCode dictionary code
     * @param dictionaryName dictionary display name
     * @param remark         remark
     */
    private static void validate(String dictionaryCode, String dictionaryName, String remark) {
        StringUtil011.requirePresent(dictionaryCode, "dictionary code", 60);

        StringUtil011.requirePresent(dictionaryName, "dictionary name", 100);

        StringUtil011.requireMax(remark, "remark", 300);
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the dictionary code.
     *
     * @return dictionary code
     */
    public String dictionaryCode() {
        return dictionaryCode;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order, smaller comes first
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the dictionary display name.
     *
     * @return dictionary name
     */
    public String dictionaryName() {
        return dictionaryName;
    }

    /**
     * Get the row status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}

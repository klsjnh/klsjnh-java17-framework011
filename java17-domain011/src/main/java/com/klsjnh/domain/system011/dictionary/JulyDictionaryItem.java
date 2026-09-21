package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryItem class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyDictionaryItem entity: one candidate value of a dictionary type, linked
 * to the master through the fixed {@code pk_mt} column.
 */

public class JulyDictionaryItem {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Master link (dictionary id).
     */
    private final String pkMt;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Item code, unique within the dictionary, immutable.
     */
    private final String itemCode;

    /**
     * Item display name.
     */
    private String itemLabel;

    /**
     * Row status: '1' enabled / '0' disabled.
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
     * @param id        primary key
     * @param pkMt      master link (dictionary id)
     * @param sortOrder manual sort order, null falls back to the default
     * @param itemCode  item code, unique within the dictionary
     * @param itemLabel item display name
     * @param status    row status
     * @param remark    remark, optional
     * @param audit     audit info
     */
    public JulyDictionaryItem(EntityId id, String pkMt, Integer sortOrder, String itemCode, String itemLabel,
            String status, String remark, AuditInfo audit) {
        this.id = id;
        this.pkMt = pkMt;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.itemCode = itemCode;
        this.itemLabel = itemLabel;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new dictionary item.
     *
     * @param id        primary key
     * @param pkMt      master link (dictionary id)
     * @param sortOrder manual sort order, null falls back to the default
     * @param itemCode  item code, unique within the dictionary, max 60
     * @param itemLabel item display name, max 100
     * @param status    row status, null falls back to enabled
     * @param remark    remark, optional, max 300
     * @param audit     audit info
     * @return new entity
     */
    public static JulyDictionaryItem create(EntityId id, String pkMt, Integer sortOrder, String itemCode,
            String itemLabel, String status, String remark, AuditInfo audit) {
        validate(pkMt, itemCode, itemLabel, remark);

        return new JulyDictionaryItem(id, pkMt, sortOrder, itemCode, itemLabel,
                status == null ? Status011.ENABLED.getCode() : status, remark, audit);
    }

    /**
     * Update the mutable fields (itemCode is immutable).
     *
     * @param itemLabel item display name
     * @param sortOrder manual sort order, null keeps the stored one
     * @param status    row status, null keeps the stored one
     * @param remark    remark, optional
     */
    public void update(String itemLabel, Integer sortOrder, String status, String remark) {
        validate(this.pkMt, this.itemCode, itemLabel, remark);
        this.itemLabel = itemLabel;
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
     * @param pkMt      master link
     * @param itemCode  item code
     * @param itemLabel item display name
     * @param remark    remark
     */
    private static void validate(String pkMt, String itemCode, String itemLabel, String remark) {
        StringUtil011.requirePresent(pkMt, "dictionary link", 33);

        StringUtil011.requirePresent(itemCode, "item code", 60);

        StringUtil011.requirePresent(itemLabel, "item label", 100);

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
     * Get the master link.
     *
     * @return dictionary id
     */
    public String pkMt() {
        return pkMt;
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
     * Get the item code.
     *
     * @return item code
     */
    public String itemCode() {
        return itemCode;
    }

    /**
     * Get the item display name.
     *
     * @return item label
     */
    public String itemLabel() {
        return itemLabel;
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

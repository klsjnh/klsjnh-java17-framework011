package com.klsjnh.infrastructure.datasource.sync.entity;

/*                JulySyncRulePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july sync rule po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Sync rule persistence PO mapped to july_sync_rule (master; BasePo011 adds
 * sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_sync_rule")
public class JulySyncRulePo extends BasePo011 {

    /** Sync code, unique, immutable. */
    private String syncCode;

    /** Sync name. */
    private String syncName;

    /** Source datasource code. */
    private String sourceDsCode;

    /** Source kind (sql / table / object). */
    private String sourceKind;

    /** Source data (sql text / table name / object name). */
    private String sourceData;

    /** Target datasource code. */
    private String targetDsCode;

    /** Target kind (table / object). */
    private String targetKind;

    /** Target data (table name / object name). */
    private String targetData;

    /** Mode (full / incr). */
    private String mode;

    /** Business key columns (target side, CSV). */
    private String syncKey;

    /** Conflict strategy (upsert / append). */
    private String conflict;

    /** JSON extension slot (reserved). */
    private String options;

    /** Page size. */
    private Integer pageSize;

    /** Remark. */
    private String remark;
}

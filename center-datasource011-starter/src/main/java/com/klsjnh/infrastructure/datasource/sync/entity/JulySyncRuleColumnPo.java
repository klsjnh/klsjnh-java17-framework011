package com.klsjnh.infrastructure.datasource.sync.entity;

/*                JulySyncRuleColumnPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july sync rule column po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Sync rule column persistence PO mapped to july_sync_rule_column (child;
 * master link column pk_mt; BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_sync_rule_column")
public class JulySyncRuleColumnPo extends BasePo011 implements MasterLinked {

    /** Master link: sync rule id (pk_mt). */
    private String pkMt;

    /** Source column. */
    private String sourceColumn;

    /** Source neutral type. */
    private String sourceType;

    /** Target column. */
    private String targetColumn;

    /** Target neutral type. */
    private String targetType;

    /** Optional value transform. */
    private String transform;

    /** Remark. */
    private String remark;

    /**
     * Get the master id.
     *
     * @return master id
     */
    @Override
    public String getPkMt() {
        return pkMt;
    }

    /**
     * Set the master id.
     *
     * @param masterId master id
     */
    @Override
    public void setPkMt(String masterId) {
        this.pkMt = masterId;
    }
}

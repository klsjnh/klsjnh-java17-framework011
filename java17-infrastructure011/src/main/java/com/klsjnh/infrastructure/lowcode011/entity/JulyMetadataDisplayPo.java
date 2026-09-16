package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataDisplayPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata display po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Display column persistence PO mapped to july_metadata_display (child, pk_mt
 * link).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata_display")
public class JulyMetadataDisplayPo extends BasePo011 implements MasterLinked {

    /** Master link. */
    private String pkMt;

    /** Column code binding a field. */
    private String displayCode;

    /** Column name. */
    private String displayName;

    /** Text alignment. */
    private String align;

    /** Column width. */
    private Integer width;

    /** Component type. */
    private String componentType;

    /** Display type code. */
    private String displayType;

    /** Extra parameter. */
    private String param011;

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

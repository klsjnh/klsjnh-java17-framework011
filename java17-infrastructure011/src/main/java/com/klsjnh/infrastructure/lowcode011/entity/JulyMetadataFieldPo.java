package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataFieldPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata field po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Field persistence PO mapped to july_metadata_field (child, pk_mt link).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata_field")
public class JulyMetadataFieldPo extends BasePo011 implements MasterLinked {

    /** Master link. */
    private String pkMt;

    /** Field code, unique within the object. */
    private String fieldCode;

    /** Field name. */
    private String fieldName;

    /** Field type code. */
    private String fieldType;

    /** Field length. */
    private Integer fieldLength;

    /** Required flag, '0' / '1'. */
    private String requiredField;

    /** Default value. */
    private String defaultValue;

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

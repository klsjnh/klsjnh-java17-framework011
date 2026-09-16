package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataServicePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata service po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Service persistence PO mapped to july_metadata_service (child, pk_mt link).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata_service")
public class JulyMetadataServicePo extends BasePo011 implements MasterLinked {

    /** Master link. */
    private String pkMt;

    /** Service code, unique within the object. */
    private String serviceCode;

    /** Service name. */
    private String serviceName;

    /** Service description. */
    private String serviceDescription;

    /** Service object type code. */
    private String objectType;

    /** Parameter type code. */
    private String paramType;

    /** SQL / script content. */
    private String serviceContent;

    /** Enabled flag, '0' / '1'. */
    private String enabled;

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

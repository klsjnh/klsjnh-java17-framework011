package com.klsjnh.infrastructure.storagecenter.storage.entity;

/*                JulyStorageProviderBucketPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july storage provider bucket po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Bucket persistence PO mapped to july_storage_provider_bucket (child of the
 * storage provider, master link column pk_mt; BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_storage_provider_bucket")
public class JulyStorageProviderBucketPo extends BasePo011 implements MasterLinked {

    /** Master link: storage provider id (pk_mt). */
    private String pkMt;

    /** Bucket code, unique within the instance, immutable. */
    private String bucketCode;

    /** Bucket name. */
    private String bucketName;

    /** Whether this is the default bucket, '0' / '1'. */
    private String isDefault;

    /** Remark, optional. */
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

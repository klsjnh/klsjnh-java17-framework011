package com.klsjnh.domain.storagecenter.storage;

/*                JulyStorageProviderBucket class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july storage provider bucket class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyStorageProviderBucket entity: one bucket of a storage instance (child of
 * july_storage_provider, master link pk_mt). The default bucket flag is
 * app-enforced to be true for at most one row per instance.
 */

public class JulyStorageProviderBucket {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Master link (storage provider id).
     */
    private final String pkMt;

    /**
     * Bucket code, unique within the instance, immutable.
     */
    private final String bucketCode;

    /**
     * Bucket name.
     */
    private String bucketName;

    /**
     * Whether this is the default bucket of the instance.
     */
    private boolean isDefault;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id         primary key
     * @param pkMt       master link (storage provider id)
     * @param bucketCode bucket code, unique within the instance
     * @param bucketName bucket name
     * @param isDefault  whether this is the default bucket
     * @param sortOrder  manual sort order, null falls back to the default
     * @param remark     remark, optional
     * @param status     row status
     * @param audit      audit info
     */
    public JulyStorageProviderBucket(EntityId id, String pkMt, String bucketCode, String bucketName, boolean isDefault,
            Integer sortOrder, String remark, String status, AuditInfo audit) {
        validate(pkMt, bucketCode, bucketName, remark);

        this.id = id;
        this.pkMt = pkMt;
        this.bucketCode = bucketCode;
        this.bucketName = bucketName;
        this.isDefault = isDefault;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new bucket.
     *
     * @param id         primary key
     * @param pkMt       master link (storage provider id)
     * @param bucketCode bucket code, unique within the instance, max 60
     * @param bucketName bucket name, max 100
     * @param isDefault  whether this is the default bucket
     * @param sortOrder  manual sort order, null falls back to the default
     * @param remark     remark, optional, max 300
     * @param audit      audit info
     * @return new entity
     */
    public static JulyStorageProviderBucket create(EntityId id, String pkMt, String bucketCode, String bucketName,
            boolean isDefault, Integer sortOrder, String remark, AuditInfo audit) {
        return new JulyStorageProviderBucket(id, pkMt, bucketCode, bucketName, isDefault, sortOrder, remark,
                Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable fields (bucketCode is immutable).
     *
     * @param bucketName bucket name
     * @param isDefault  whether this is the default bucket, null keeps the stored one
     * @param sortOrder  manual sort order, null keeps the stored one
     * @param status     row status, null keeps the stored one
     * @param remark     remark, optional
     */
    public void update(String bucketName, Boolean isDefault, Integer sortOrder, String status, String remark) {
        validate(this.pkMt, this.bucketCode, bucketName, remark);

        this.bucketName = bucketName;
        this.remark = remark;

        if (isDefault != null) {
            this.isDefault = isDefault;
        }

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
     * @param pkMt       master link
     * @param bucketCode bucket code
     * @param bucketName bucket name
     * @param remark     remark
     */
    private static void validate(String pkMt, String bucketCode, String bucketName, String remark) {
        StringUtil011.requirePresent(pkMt, "storage provider link", 33);

        StringUtil011.requirePresent(bucketCode, "bucket code", 60);

        StringUtil011.requirePresent(bucketName, "bucket name", 100);

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
     * @return storage provider id
     */
    public String pkMt() {
        return pkMt;
    }

    /**
     * Get the bucket code.
     *
     * @return bucket code
     */
    public String bucketCode() {
        return bucketCode;
    }

    /**
     * Get the bucket name.
     *
     * @return bucket name
     */
    public String bucketName() {
        return bucketName;
    }

    /**
     * Whether this is the default bucket.
     *
     * @return true when default
     */
    public boolean isDefault() {
        return isDefault;
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
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
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
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}

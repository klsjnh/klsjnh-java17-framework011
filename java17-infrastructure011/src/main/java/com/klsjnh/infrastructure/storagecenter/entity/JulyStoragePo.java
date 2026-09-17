package com.klsjnh.infrastructure.storagecenter.entity;

/*                JulyStoragePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Storage instance persistence PO mapped to july_storage, a sorted table
 * (BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_storage")
public class JulyStoragePo extends BasePo011 {

    /** Storage code, unique, immutable. */
    private String storageCode;

    /** Storage instance display name. */
    private String storageName;

    /** Storage type code (local011 / minio011 / ...). */
    private String provider;

    /** Local root directory (local011). */
    private String basePath;

    /** Endpoint (S3 family). */
    private String endpoint;

    /** Access key (S3 family). */
    private String accessKey;

    /** Secret key (S3 family), never echoed back. */
    private String secretKey;

    /** Whether to use HTTPS, '0' / '1'. */
    private String secure;

    /** Default bucket. */
    private String defaultBucket;

    /** Presigned URL expiry, seconds. */
    private Integer presignExpirySeconds;

    /** Remark, optional. */
    private String remark;
}

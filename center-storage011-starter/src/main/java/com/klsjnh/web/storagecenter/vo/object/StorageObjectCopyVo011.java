package com.klsjnh.web.storagecenter.vo.object;

/*                StorageObjectCopyVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  storage object copy vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Copy an object within / across buckets.
 */

@Data
public class StorageObjectCopyVo011 {

    /** Storage code, optional (blank = default instance). */
    @Schema(description = "存储实例编码（留空用默认）")
    private String storageCode;

    /** Source bucket. */
    @Schema(description = "源桶", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;

    /** Source object. */
    @Schema(description = "源对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;

    /** Target bucket. */
    @Schema(description = "目标桶", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetBucket;

    /** Target object. */
    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetName;
}

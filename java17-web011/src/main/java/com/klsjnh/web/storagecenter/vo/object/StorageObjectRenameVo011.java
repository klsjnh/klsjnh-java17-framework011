package com.klsjnh.web.storagecenter.vo.object;

/*                StorageObjectRenameVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  storage object rename vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Rename (move) an object within the same bucket.
 */

@Data
public class StorageObjectRenameVo011 {

    /** Storage code, optional (blank = default instance). */
    @Schema(description = "存储实例编码（留空用默认）")
    private String storageCode;

    /** Bucket. */
    @Schema(description = "桶", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;

    /** Source object. */
    @Schema(description = "源对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;

    /** Target object. */
    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetName;
}

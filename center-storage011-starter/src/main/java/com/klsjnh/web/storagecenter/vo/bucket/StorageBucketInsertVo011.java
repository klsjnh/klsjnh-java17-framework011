package com.klsjnh.web.storagecenter.vo.bucket;

/*                StorageBucketInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Create bucket request VO.
 */

@Data
public class StorageBucketInsertVo011 {

    /** Storage code, blank for the default instance. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Bucket code, unique within the instance, immutable. */
    @Schema(description = "桶编码（同实例内唯一，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketCode;

    /** Bucket name. */
    @Schema(description = "桶名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;

    /** Whether this becomes the default bucket. */
    @Schema(description = "是否设为默认桶")
    private Boolean isDefault;

    /** Region, ignored by local / MinIO. */
    @Schema(description = "区域（local/MinIO 忽略）")
    private String region;
}

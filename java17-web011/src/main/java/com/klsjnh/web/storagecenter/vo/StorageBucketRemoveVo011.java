package com.klsjnh.web.storagecenter.vo;

/*                StorageBucketRemoveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket remove vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Remove bucket request VO.
 */

@Data
public class StorageBucketRemoveVo011 {

    /** Storage code. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Bucket name. */
    @Schema(description = "桶名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;
}

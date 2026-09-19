package com.klsjnh.web.storagecenter.vo.object;

/*                StorageObjectRefVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object ref vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Single object reference request VO.
 */

@Data
public class StorageObjectRefVo011 {

    /** Storage code, blank for the default instance. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Bucket name, blank for the default bucket. */
    @Schema(description = "桶名（留空用默认桶）")
    private String bucketName;

    /** Object name. */
    @Schema(description = "对象名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;
}

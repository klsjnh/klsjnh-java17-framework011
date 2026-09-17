package com.klsjnh.web.storagecenter.vo;

/*                StorageObjectBatchRemoveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object batch remove vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Batch remove request VO.
 */

@Data
public class StorageObjectBatchRemoveVo011 {

    /** Storage code, blank for the default instance. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Bucket name. */
    @Schema(description = "桶名（留空用默认桶）")
    private String bucketName;

    /** Object names. */
    @Schema(description = "对象名列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> objectNames;
}

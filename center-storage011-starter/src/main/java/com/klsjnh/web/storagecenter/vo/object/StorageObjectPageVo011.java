package com.klsjnh.web.storagecenter.vo.object;

/*                StorageObjectPageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  storage object native page vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Native paged object listing (delimiter + marker).
 */

@Data
public class StorageObjectPageVo011 {

    /** Storage code, optional (blank = default instance). */
    @Schema(description = "存储实例编码（留空用默认）")
    private String storageCode;

    /** Bucket. */
    @NotBlank(message = "bucketName is required")
    @Schema(description = "桶", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;

    /** Key prefix, optional. */
    @Schema(description = "前缀（可选）")
    private String prefix;

    /** Directory delimiter (e.g. /), optional for a flat listing. */
    @Schema(description = "目录分隔符（如 /；留空为平铺）")
    private String delimiter;

    /** Page size (clamped to [1, 1000]). */
    @Schema(description = "每页条数（夹取 [1,1000]）")
    private Integer limit;

    /** Continue-after marker, optional (from the previous page). */
    @Schema(description = "续传标记（上一页的 nextMarker；首页留空）")
    private String marker;
}

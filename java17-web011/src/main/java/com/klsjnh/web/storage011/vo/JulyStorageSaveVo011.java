package com.klsjnh.web.storage011.vo;

/*                JulyStorageSaveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage save vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert / update request VO for a storage instance.
 */

@Data
public class JulyStorageSaveVo011 {

    /** Primary key, null on insert. */
    @Schema(description = "主键（新增不传，修改必传）")
    private String id;

    /** Storage code, unique, immutable. */
    @Schema(description = "存储编码（唯一，不可变）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageCode;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Display name. */
    @Schema(description = "存储名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageName;

    /** Storage type code. */
    @Schema(description = "存储类型（local011/minio011/cos011/tos011/oss011/s3011）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String provider;

    /** Local root directory (local011). */
    @Schema(description = "本地根目录（local011 必填）")
    private String basePath;

    /** Endpoint (S3 family). */
    @Schema(description = "Endpoint（S3 系必填）")
    private String endpoint;

    /** Access key. */
    @Schema(description = "Access Key")
    private String accessKey;

    /** Secret key, write-only; blank on update keeps the stored one. */
    @Schema(description = "Secret Key（出参不回显；修改留空保持原值）")
    private String secretKey;

    /** Whether to use HTTPS. */
    @Schema(description = "是否 HTTPS")
    private Boolean secure;

    /** Default bucket. */
    @Schema(description = "默认桶")
    private String defaultBucket;

    /** Presigned URL expiry seconds. */
    @Schema(description = "预签名有效期（秒）")
    private Integer presignExpirySeconds;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status (update only). */
    @Schema(description = "状态（0 停用 / 1 启用，修改可传）")
    private String status;
}

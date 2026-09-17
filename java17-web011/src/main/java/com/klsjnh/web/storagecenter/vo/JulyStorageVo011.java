package com.klsjnh.web.storagecenter.vo;

/*                JulyStorageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Storage instance response VO, aligned with the legacy payload: access /
 * secret keys are masked ({@code ******}) and {@code secure} is the
 * {@code "1"} / {@code "0"} string code.
 */

@Data
public class JulyStorageVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Storage code, unique, immutable. */
    @Schema(description = "存储编码（唯一，不可变）")
    private String storageCode;

    /** Manual sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Display name. */
    @Schema(description = "存储名称")
    private String storageName;

    /** Storage type code. */
    @Schema(description = "存储类型")
    private String provider;

    /** Local root directory. */
    @Schema(description = "本地根目录")
    private String basePath;

    /** Endpoint. */
    @Schema(description = "Endpoint")
    private String endpoint;

    /** Access key (masked). */
    @Schema(description = "Access Key（打码 ******）")
    private String accessKey;

    /** Secret key (masked). */
    @Schema(description = "Secret Key（打码 ******）")
    private String secretKey;

    /** Whether to use HTTPS ("1" yes / "0" no). */
    @Schema(description = "是否 HTTPS（1 是 / 0 否）")
    private String secure;

    /** Default bucket. */
    @Schema(description = "默认桶")
    private String defaultBucket;

    /** Presigned URL expiry seconds. */
    @Schema(description = "预签名有效期（秒）")
    private Integer presignExpirySeconds;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Creator. */
    @Schema(description = "创建人")
    private String createdBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updatedBy;

    /** Create date. */
    @Schema(description = "创建日期")
    private LocalDateTime createDate;

    /** Last modify date. */
    @Schema(description = "最后修改日期")
    private LocalDateTime modifyDate;
}

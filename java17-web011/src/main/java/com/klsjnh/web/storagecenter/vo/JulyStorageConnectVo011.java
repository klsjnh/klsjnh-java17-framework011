package com.klsjnh.web.storagecenter.vo;

/*                JulyStorageConnectVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage connect vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Connectivity probe request VO: with an id the saved instance is probed;
 * without it the draft fields are probed.
 */

@Data
public class JulyStorageConnectVo011 {

    /** Saved storage id, optional. */
    @Schema(description = "已保存实例 id（传则测已保存；不传则测草稿）")
    private String id;

    /** Storage type code (draft). */
    @Schema(description = "存储类型（草稿）")
    private String provider;

    /** Local root (draft). */
    @Schema(description = "本地根目录（草稿）")
    private String basePath;

    /** Endpoint (draft). */
    @Schema(description = "Endpoint（草稿）")
    private String endpoint;

    /** Access key (draft). */
    @Schema(description = "Access Key（草稿）")
    private String accessKey;

    /** Secret key (draft). */
    @Schema(description = "Secret Key（草稿）")
    private String secretKey;

    /** Whether to use HTTPS (draft). */
    @Schema(description = "是否 HTTPS（草稿）")
    private Boolean secure;

    /** Default bucket (draft). */
    @Schema(description = "默认桶（草稿）")
    private String defaultBucket;

    /** Presigned URL expiry seconds (draft). */
    @Schema(description = "预签名有效期（秒，草稿）")
    private Integer presignExpirySeconds;
}

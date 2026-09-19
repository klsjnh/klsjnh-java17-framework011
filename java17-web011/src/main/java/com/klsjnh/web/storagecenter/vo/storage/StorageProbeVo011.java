package com.klsjnh.web.storagecenter.vo.storage;

/*                StorageProbeVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  connection probe response vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Connectivity probe response, legacy-aligned: success flag + bucket count +
 * the location ({@code basePath} for the local adapter / {@code endpoint} for
 * the S3 family) + message. Null locations are omitted, so a local probe
 * carries only {@code basePath} and a remote probe only {@code endpoint}.
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageProbeVo011 {

    /** Whether the storage answered. */
    @Schema(description = "是否连通")
    private Boolean success;

    /** Bucket count seen by the probe. */
    @Schema(description = "桶数量")
    private Integer bucketCount;

    /** Local base path (local011). */
    @Schema(description = "本地根目录（local011）")
    private String basePath;

    /** Remote endpoint (S3 / MinIO). */
    @Schema(description = "Endpoint（S3/MinIO）")
    private String endpoint;

    /** Detail: "ok" on success, the failure reason otherwise. */
    @Schema(description = "说明（ok / 失败原因）")
    private String message;
}

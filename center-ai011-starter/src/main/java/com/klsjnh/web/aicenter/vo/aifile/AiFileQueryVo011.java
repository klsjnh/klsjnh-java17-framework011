package com.klsjnh.web.aicenter.vo.aifile;

/*                AiFileQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file lookup request vo
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Provider file lookup request (get / delete / content).
 */

@Data
public class AiFileQueryVo011 {

    /** Provider code, alternative to providerId. */
    @Schema(description = "模型提供方编码（与 providerId 二选一）")
    private String provider;

    /** Provider id, alternative to provider. */
    @Schema(description = "模型提供方 id（与 provider 二选一）")
    private String providerId;

    /** Api key code; blank for the default enabled key. */
    @Schema(description = "密钥编码（留空用默认启用密钥）")
    private String api;

    /** Api key id, alternative to api. */
    @Schema(description = "密钥 id（与 api 二选一）")
    private String apiId;

    /** Provider file id. */
    @NotBlank(message = "fileId is required")
    @Schema(description = "厂商文件 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;
}

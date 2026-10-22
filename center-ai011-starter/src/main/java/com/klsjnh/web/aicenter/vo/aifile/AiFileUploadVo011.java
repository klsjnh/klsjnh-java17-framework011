package com.klsjnh.web.aicenter.vo.aifile;

/*                AiFileUploadVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file upload request vo
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Provider file upload request: bytes (base64) or a remote URL.
 */

@Data
public class AiFileUploadVo011 {

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

    /** File name. */
    @Schema(description = "文件名")
    private String fileName;

    /** Purpose: storage / file-extract / retrieval-text / retrieval-image / assistants / batch / fine-tune / vision. */
    @NotBlank(message = "purpose is required")
    @Schema(description = "用途（storage / file-extract / retrieval-text / retrieval-image / assistants / batch / fine-tune / vision）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    /** File bytes as base64. */
    @Schema(description = "文件内容（base64；与 url 二选一）")
    private String fileBase64;

    /** Remote file URL. */
    @Schema(description = "远程文件 URL（与 fileBase64 二选一）")
    private String url;
}

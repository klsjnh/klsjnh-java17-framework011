package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderApiInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Insert request VO for an api key (providerCode locates the master).
 */

@Data
public class AiModelProviderApiInsertVo011 {

    /** Provider code locating the master. */
    @NotBlank(message = "providerCode is required")
    @Schema(description = "提供商编码（定位主表）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Api key code, unique within the provider, max 60. */
    @NotBlank(message = "apiCode is required")
    @Schema(description = "密钥编码（同提供商内唯一，最长 60，创建后不可修改）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiCode;

    /** Api key display name, max 100. */
    @NotBlank(message = "apiName is required")
    @Schema(description = "密钥名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiName;

    /** Api key secret, max 300 plaintext; write-only, never echoed back (stored as enc:v1:). */
    @NotBlank(message = "apiKey is required")
    @Schema(description = "API Key（明文最长 300，落库密文；出参不回显）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}

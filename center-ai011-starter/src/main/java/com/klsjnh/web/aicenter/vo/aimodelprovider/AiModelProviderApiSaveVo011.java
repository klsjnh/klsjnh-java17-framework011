package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderApiSaveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  whole-save row VO without providerCode (master link comes
 *                  from the saveWhole request; the standalone insert keeps it)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Save item for one api key inside the whole save (saveWhole): the master link
 * comes from the request, so there is no {@code providerCode} here. The
 * standalone {@code /insertApi} endpoint keeps using
 * {@link AiModelProviderApiInsertVo011}, where {@code providerCode} locates the
 * master and stays required.
 */

@Data
public class AiModelProviderApiSaveVo011 {

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

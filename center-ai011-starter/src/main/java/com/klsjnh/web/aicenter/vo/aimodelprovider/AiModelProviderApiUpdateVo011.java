package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderApiUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Update request VO for an api key (apiCode immutable).
 */

@Data
public class AiModelProviderApiUpdateVo011 {

    /** Primary key. */
    @NotBlank(message = "id is required")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Api key display name, max 100. */
    @NotBlank(message = "apiName is required")
    @Schema(description = "密钥名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiName;

    /** Manual sort order, smaller comes first; blank keeps the stored one. */
    @Schema(description = "排序（越小越靠前，留空保持原值）")
    private Integer sortOrder;

    /** Api key secret, max 300 plaintext; BLANK keeps the stored one (stored as enc:v1:). */
    @Schema(description = "API Key（明文最长 300；留空保持原值，落库密文；出参不回显）")
    private String apiKey;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用，留空保持原值）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}

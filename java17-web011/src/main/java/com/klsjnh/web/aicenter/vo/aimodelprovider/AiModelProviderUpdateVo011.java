package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Update request VO for a provider (providerCode immutable).
 */

@Data
public class AiModelProviderUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Provider display name, max 100. */
    @Schema(description = "提供商名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerName;

    /** Manual sort order, smaller comes first; blank keeps the stored one. */
    @Schema(description = "排序（越小越靠前，留空保持原值）")
    private Integer sortOrder;

    /** Base url, max 300. */
    @Schema(description = "接口 Base URL（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String baseUrl;

    /** Comma separated model list, max 500. */
    @Schema(description = "模型清单（逗号分隔，最长 500）")
    private String models;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用，留空保持原值）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}

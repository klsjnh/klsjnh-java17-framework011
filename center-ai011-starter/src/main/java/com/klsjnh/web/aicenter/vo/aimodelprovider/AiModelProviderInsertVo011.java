package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Insert request VO for a provider (providerCode required and immutable).
 */

@Data
public class AiModelProviderInsertVo011 {

    /** Provider code, unique, immutable, max 60. */
    @NotBlank(message = "providerCode is required")
    @Schema(description = "提供商编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Provider display name, max 100. */
    @NotBlank(message = "providerName is required")
    @Schema(description = "提供商名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerName;

    /** Base url, max 300. */
    @NotBlank(message = "baseUrl is required")
    @Schema(description = "接口 Base URL（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String baseUrl;

    /** Comma separated model list, max 500. */
    @Schema(description = "模型清单（逗号分隔，最长 500）")
    private String models;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}

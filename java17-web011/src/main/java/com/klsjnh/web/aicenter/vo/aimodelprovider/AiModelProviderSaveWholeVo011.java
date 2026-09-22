package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderSaveWholeVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  ai model provider save whole vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Whole save request (provider + api keys): the provider is inserted when id is
 * blank and updated otherwise, then the api list replaces the old children.
 * <p>
 * Each api row reuses {@link AiModelProviderApiInsertVo011}; its
 * {@code providerCode} is ignored — the master link always comes from this
 * request.
 * </p>
 */

@Data
public class AiModelProviderSaveWholeVo011 {

    /** Provider id; blank inserts a new provider. */
    @Schema(description = "提供商主键（留空=新增；非空=修改）")
    private String id;

    /** Provider code, unique, immutable; used on insert only. */
    @Schema(description = "提供商编码（全局唯一，创建后不可修改；仅新增时用）")
    private String providerCode;

    /** Sort order, smaller comes first. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Provider name, max 100. */
    @Schema(description = "提供商名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerName;

    /** Interface base url, max 300. */
    @Schema(description = "接口 Base URL（OpenAI 兼容，最长 300）")
    private String baseUrl;

    /** Model list, comma separated. */
    @Schema(description = "模型清单（逗号分隔）")
    private String models;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;

    /** Api key rows replacing the old children; blank means none. */
    @Schema(description = "密钥列表（整存替换：旧子表逻辑删 + 新列表插入）")
    private List<AiModelProviderApiInsertVo011> apis = new ArrayList<>();
}

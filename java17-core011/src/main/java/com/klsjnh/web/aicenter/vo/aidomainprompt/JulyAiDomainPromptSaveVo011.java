package com.klsjnh.web.aicenter.vo.aidomainprompt;

/*                JulyAiDomainPromptSaveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain prompt save vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Save item for one prompt inside the whole save (saveWhole): the master link
 * comes from the parent, so there is no pkMt here.
 */

@Data
public class JulyAiDomainPromptSaveVo011 {

    /** Prompt code, unique, immutable. */
    @Schema(description = "提示词编码（全局唯一，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptCode;

    /** Prompt name, max 100. */
    @Schema(description = "提示词名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptName;

    /** Scene (inference / image / tts), classification only. */
    @Schema(description = "适用能力（inference / image / tts，仅分类）")
    private String scene;

    /** Content mode (inline / storage). */
    @Schema(description = "内容模式（inline 正文入库 / storage 正文落对象存储）")
    private String contentMode;

    /** Inline content; in storage mode the body to persist. */
    @Schema(description = "正文（inline 时直接入库；storage 时作为落对象存储的内容）")
    private String content;

    /** Storage instance code, storage mode only. */
    @Schema(description = "存储实例（storage 模式；缺省=默认实例）")
    private String storageCode;

    /** Bucket, storage mode only. */
    @Schema(description = "桶（storage 模式；缺省=ai-prompt）")
    private String bucket;

    /** Variable declarations, nullable. */
    @Schema(description = "变量声明（可空）")
    private String variables;

    /** Sort order, smaller comes first; blank falls back to the list position. */
    @Schema(description = "排序（越小越靠前；留空按列表顺序）")
    private Integer sortOrder;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Row status: 0 disabled / 1 enabled; blank falls back to enabled. */
    @Schema(description = "状态（0 停用 / 1 启用；留空默认启用）")
    private String status;
}

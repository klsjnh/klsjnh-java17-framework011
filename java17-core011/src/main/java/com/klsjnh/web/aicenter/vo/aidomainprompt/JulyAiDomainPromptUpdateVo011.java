package com.klsjnh.web.aicenter.vo.aidomainprompt;

/*                JulyAiDomainPromptUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt update request (promptCode is immutable).
 */

@Data
public class JulyAiDomainPromptUpdateVo011 {

    /** Prompt id. */
    @Schema(description = "提示词 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Prompt name. */
    @Schema(description = "提示词名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptName;

    /** Scene. */
    @Schema(description = "适用能力（inference / image / tts）")
    private String scene;

    /** Content mode (inline / storage). */
    @Schema(description = "内容模式（inline / storage，默认 inline）")
    private String contentMode;

    /** Inline content (TEXT). */
    @Schema(description = "正文（inline 时；TEXT≈2.1万汉字）")
    private String content;

    /** Storage instance code (storage mode). */
    @Schema(description = "存储实例（storage 时；缺省=默认实例）")
    private String storageCode;

    /** Bucket (storage mode). */
    @Schema(description = "桶（storage 时；约定 ai-prompt）")
    private String bucket;

    /** Variable declarations. */
    @Schema(description = "变量声明（可空）")
    private String variables;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;
}

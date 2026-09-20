package com.klsjnh.web.aicenter.vo.aiprompt;

/*                JulyAiPromptUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt update request (promptCode is immutable).
 */

@Data
public class JulyAiPromptUpdateVo011 {

    /** Prompt id. */
    @Schema(description = "提示词 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Prompt name. */
    @Schema(description = "提示词名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptName;

    /** Scene. */
    @Schema(description = "适用能力（inference / image / tts）")
    private String scene;

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

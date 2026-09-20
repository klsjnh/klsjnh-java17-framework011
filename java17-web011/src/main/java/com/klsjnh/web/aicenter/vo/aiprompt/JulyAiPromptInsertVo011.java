package com.klsjnh.web.aicenter.vo.aiprompt;

/*                JulyAiPromptInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Prompt insert request: the prompt header plus its per-domain details.
 */

@Data
public class JulyAiPromptInsertVo011 {

    /** Prompt code, globally unique. */
    @Schema(description = "提示词编码（全局唯一，不可变）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptCode;

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

    /** Domain details. */
    @Schema(description = "业务域明细（每域一份内容）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<JulyAiPromptDetailVo011> details;
}

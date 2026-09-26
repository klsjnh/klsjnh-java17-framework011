package com.klsjnh.web.aicenter.vo.aidomainprompt;

/*                JulyAiDomainPromptRenderVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt render vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Prompt render request: which prompt (globally unique code) and the variable
 * values.
 */

@Data
public class JulyAiDomainPromptRenderVo011 {

    /** Prompt code. */
    @NotBlank(message = "promptCode is required")
    @Schema(description = "提示词编码（全局唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptCode;

    /** Variable values. */
    @Schema(description = "变量值（${var} 替换）")
    private Map<String, String> params;
}

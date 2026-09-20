package com.klsjnh.web.aicenter.vo.aiprompt;

/*                JulyAiPromptRenderVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt render vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Prompt render request: which prompt, which domain and the variable values.
 */

@Data
public class JulyAiPromptRenderVo011 {

    /** Prompt code. */
    @Schema(description = "提示词编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptCode;

    /** Business domain, blank for the default. */
    @Schema(description = "业务域（留空用默认）")
    private String domainCode;

    /** Variable values. */
    @Schema(description = "变量值（${var} 替换）")
    private Map<String, String> params;
}

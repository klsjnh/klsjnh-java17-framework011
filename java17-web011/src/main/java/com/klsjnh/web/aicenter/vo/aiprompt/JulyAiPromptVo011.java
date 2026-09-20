package com.klsjnh.web.aicenter.vo.aiprompt;

/*                JulyAiPromptVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt response.
 */

@Data
public class JulyAiPromptVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Prompt code. */
    @Schema(description = "提示词编码")
    private String promptCode;

    /** Prompt name. */
    @Schema(description = "提示词名称")
    private String promptName;

    /** Scene. */
    @Schema(description = "适用能力")
    private String scene;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;
}

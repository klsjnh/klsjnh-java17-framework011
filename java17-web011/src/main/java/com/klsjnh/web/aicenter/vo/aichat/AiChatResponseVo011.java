package com.klsjnh.web.aicenter.vo.aichat;

/*                AiChatResponseVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat response vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Chat response: resolved provider / api / model, assistant content and usage.
 * The api key is never returned.
 */

@Data
public class AiChatResponseVo011 {

    /** Resolved provider code. */
    @Schema(description = "提供方编码")
    private String providerCode;

    /** Resolved api code. */
    @Schema(description = "密钥编码（非密钥本身）")
    private String apiCode;

    /** Resolved model. */
    @Schema(description = "模型名")
    private String model;

    /** Assistant content. */
    @Schema(description = "模型回复")
    private String content;

    /** Finish reason. */
    @Schema(description = "结束原因")
    private String finishReason;

    /** Prompt tokens. */
    @Schema(description = "输入 token")
    private Integer promptTokens;

    /** Completion tokens. */
    @Schema(description = "输出 token")
    private Integer completionTokens;

    /** Total tokens. */
    @Schema(description = "总 token")
    private Integer totalTokens;
}

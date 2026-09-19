package com.klsjnh.web.ai011.vo.aichat;

/*                AiChatRequestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat request vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Chat request: provider / api may be an id or a code; api and model are
 * optional (defaults). Messages are required.
 */

@Data
public class AiChatRequestVo011 {

    /** Provider id or providerCode (e.g. longcat or its id). */
    @Schema(description = "模型提供方（id 或 providerCode，如 longcat）")
    private String provider;

    /** Api key id or apiCode; blank for the default enabled key. */
    @Schema(description = "密钥（id 或 apiCode；留空用默认启用密钥）")
    private String api;

    /** Model name; blank for the provider's first model. */
    @Schema(description = "模型名（必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    /** Chat messages. */
    @Schema(description = "消息列表")
    private List<AiChatMessageVo011> messages;

    /** Sampling temperature, optional. */
    @Schema(description = "温度（可选）")
    private Double temperature;

    /** Max output tokens, optional. */
    @Schema(description = "最大输出 token（可选）")
    private Integer maxTokens;
}

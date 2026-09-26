package com.klsjnh.web.aicenter.vo.aichat;

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

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Chat request: provider / api may be an id or a code; api and model are
 * optional (defaults). Messages are required.
 */

@Data
public class AiChatRequestVo011 {

    /** Provider code (providerCode), alternative to providerId. */
    @Schema(description = "模型提供方编码（providerCode，如 longcat）")
    private String provider;

    /** Provider id, alternative to provider. */
    @Schema(description = "模型提供方 id（与 provider 二选一）")
    private String providerId;

    /** Api key code (apiCode); blank for the default enabled key. */
    @Schema(description = "密钥编码（apiCode；留空用默认启用密钥）")
    private String api;

    /** Api key id, alternative to api. */
    @Schema(description = "密钥 id（与 api 二选一）")
    private String apiId;

    /** Model name, required. */
    @NotBlank(message = "model is required")
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

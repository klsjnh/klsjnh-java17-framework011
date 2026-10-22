package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderChatTestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  chat probe request vo (provider + model)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Chat (model-level) probe request VO: proves /chat/completions works for one
 * model with the provider's default key.
 */

@Data
public class AiModelProviderChatTestVo011 {

    /** Provider code. */
    @NotBlank(message = "providerCode is required")
    @Schema(description = "提供商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerCode;

    /** Model name. */
    @NotBlank(message = "model is required")
    @Schema(description = "模型名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;
}

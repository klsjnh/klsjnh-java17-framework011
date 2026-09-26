package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderTestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider test vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Provider level connectivity probe request VO.
 */

@Data
public class AiModelProviderTestVo011 {

    /** Provider code. */
    @NotBlank(message = "providerCode is required")
    @Schema(description = "提供商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerCode;
}

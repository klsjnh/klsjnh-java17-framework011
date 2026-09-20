package com.klsjnh.web.aicenter.vo.aiimage;

/*                AiImageRequestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image request vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Image generation request: provider / key may be an id or a code; an input
 * image (url or base64) turns it into image-to-image.
 */

@Data
public class AiImageRequestVo011 {

    /** Provider id or providerCode (e.g. stepfun). */
    @Schema(description = "模型提供方（id 或 providerCode，如 stepfun）")
    private String provider;

    /** Provider id, alternative to provider. */
    @Schema(description = "模型提供方 id（与 provider 二选一）")
    private String providerId;

    /** Api key id or apiCode; blank for the default enabled key. */
    @Schema(description = "密钥（id 或 apiCode；留空用默认启用密钥）")
    private String api;

    /** Api key id, alternative to api. */
    @Schema(description = "密钥 id（与 api 二选一）")
    private String apiId;

    /** Model name; blank for the provider default. */
    @Schema(description = "模型名（必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    /** Artifact return form: url / bytes / b64; blank for provider default. */
    @Schema(description = "返回形态（url / bytes / b64；留空用默认）")
    private String returnType;

    /** Text prompt. */
    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    /** Output size (e.g. 1024x1024), optional. */
    @Schema(description = "输出尺寸（如 1024x1024）")
    private String size;

    /** Inference steps, optional. */
    @Schema(description = "生成步数")
    private Integer steps;

    /** Random seed, optional. */
    @Schema(description = "随机种子")
    private Long seed;

    /** Guidance scale, optional. */
    @Schema(description = "引导强度")
    private Double guidanceScale;

    /** Negative prompt, optional. */
    @Schema(description = "负面提示词")
    private String negativePrompt;

    /** Input image url (image-to-image), optional. */
    @Schema(description = "输入图 URL（图生图，可选）")
    private String imageUrl;

    /** Input image base64 (image-to-image), optional. */
    @Schema(description = "输入图 base64（图生图，可选）")
    private String imageBase64;
}

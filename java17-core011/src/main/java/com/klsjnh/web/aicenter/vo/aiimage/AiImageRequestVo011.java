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
 *      2026.09.24  document text / image / text+image modes
 *      2026.09.24  require storageCode|storageId + bucketCode|bucketId
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Image generation request: provider / key may be an id or a code. Modes:
 * prompt only = text-to-image; prompt + imageUrl/imageBase64 = image-to-image /
 * text+image-to-image. Persistence requires an explicit storage locator
 * (no framework default).
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

    /** Text prompt (required; also the edit instruction when an input image is set). */
    @Schema(description = "提示词（必填；有输入图时为编辑/图生图指令）", requiredMode = Schema.RequiredMode.REQUIRED)
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

    /** Input image url (image-to-image / text+image-to-image), optional. */
    @Schema(description = "输入图 URL（有则图生图/文图生图；可选）")
    private String imageUrl;

    /** Input image base64 (image-to-image / text+image-to-image), optional. */
    @Schema(description = "输入图 base64（有则图生图/文图生图；可选）")
    private String imageBase64;

    /** Storage instance code (preferred). Required with bucket unless storageId is set. */
    @Schema(description = "存储实例 code（优先；与 storageId 二选一必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageCode;

    /** Storage instance id, alternative to storageCode. */
    @Schema(description = "存储实例 id（与 storageCode 二选一）")
    private String storageId;

    /** Bucket code within the instance (preferred). Required with storage unless bucketId is set. */
    @Schema(description = "桶 code（优先；与 bucketId 二选一必传；缺参 400，不走默认桶）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketCode;

    /** Bucket id, alternative to bucketCode. */
    @Schema(description = "桶 id（与 bucketCode 二选一）")
    private String bucketId;
}

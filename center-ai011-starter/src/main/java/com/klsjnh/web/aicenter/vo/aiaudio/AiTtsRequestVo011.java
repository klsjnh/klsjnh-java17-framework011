package com.klsjnh.web.aicenter.vo.aiaudio;

/*                AiTtsRequestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts request vo 011 class
 *      2026.09.24  require storageCode|storageId + bucketCode|bucketId
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Speech synthesis request: provider / key may be an id or a code. Persistence
 * requires an explicit storage locator (no framework default).
 */

@Data
public class AiTtsRequestVo011 {

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
    @NotBlank(message = "model is required")
    @Schema(description = "模型名（必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    /** Artifact return form: url / bytes / b64; blank for provider default. */
    @Schema(description = "返回形态（url / bytes / b64；留空用默认）")
    private String returnType;

    /** Text to synthesize. */
    @NotBlank(message = "input is required")
    @Schema(description = "待合成文本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String input;

    /** Voice id / name, optional. */
    @Schema(description = "音色（留空用默认音色）")
    private String voice;

    /** Style / emotion instruction, optional. */
    @Schema(description = "介绍 / 风格指令（可选）")
    private String instruction;

    /** Speed multiplier, optional. */
    @Schema(description = "语速（0.5~2）")
    private Double speed;

    /** Volume multiplier, optional. */
    @Schema(description = "音量（0.1~2.0）")
    private Double volume;

    /** Audio format (mp3 / wav / ...), optional. */
    @Schema(description = "音频格式（mp3 / wav / ...）")
    private String format;

    /** Sample rate in Hz, optional. */
    @Schema(description = "采样率（Hz）")
    private Integer sampleRate;

    /** Storage instance code (preferred). Required with bucket unless storageId is set. */
    @NotBlank(message = "storageCode is required")
    @Schema(description = "存储实例 code（优先；与 storageId 二选一必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageCode;

    /** Storage instance id, alternative to storageCode. */
    @Schema(description = "存储实例 id（与 storageCode 二选一）")
    private String storageId;

    /** Bucket code within the instance (preferred). Required with storage unless bucketId is set. */
    @NotBlank(message = "bucketCode is required")
    @Schema(description = "桶 code（优先；与 bucketId 二选一必传；缺参 400，不走默认桶）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketCode;

    /** Bucket id, alternative to bucketCode. */
    @Schema(description = "桶 id（与 bucketCode 二选一）")
    private String bucketId;
}

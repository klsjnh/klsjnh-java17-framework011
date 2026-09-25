package com.klsjnh.web.aicenter.vo.aiaudio;

/*                AiAsrRequestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr request vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Speech recognition request: provider / key may be an id or a code; the audio
 * is supplied as base64 or a url.
 */

@Data
public class AiAsrRequestVo011 {

    /** Provider id or providerCode. */
    @Schema(description = "模型提供方（id 或 providerCode）")
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

    /** Model name, required. */
    @Schema(description = "模型名（必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    /** Audio base64 (alternative to audioUrl). */
    @Schema(description = "音频 base64（与 audioUrl 二选一）")
    private String audioBase64;

    /** Audio url (alternative to audioBase64). */
    @Schema(description = "音频 URL（与 audioBase64 二选一）")
    private String audioUrl;

    /** Audio format (mp3 / wav / ...), optional. */
    @Schema(description = "音频格式（mp3 / wav / ...）")
    private String format;

    /** Language hint, optional. */
    @Schema(description = "语言提示（可选）")
    private String language;
}

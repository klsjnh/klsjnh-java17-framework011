package com.klsjnh.web.ai011.vo.aitts;

/*                AiTtsResponseVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts response vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Speech synthesis response: the audio artifact in the requested form (url /
 * base64). Raw bytes are exposed as base64.
 */

@Data
public class AiTtsResponseVo011 {

    /** Artifact form: url / bytes / b64. */
    @Schema(description = "返回形态（url / bytes / b64）")
    private String returnType;

    /** Download url when the form is url. */
    @Schema(description = "下载地址（url 形态）")
    private String url;

    /** Base64 content when the form is b64 or bytes. */
    @Schema(description = "base64 内容（b64 / bytes 形态）")
    private String base64;

    /** Media mime type. */
    @Schema(description = "媒体类型（如 audio/mpeg）")
    private String mimeType;
}

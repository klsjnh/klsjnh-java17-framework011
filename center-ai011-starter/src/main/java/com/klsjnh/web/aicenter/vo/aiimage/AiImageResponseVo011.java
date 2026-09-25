package com.klsjnh.web.aicenter.vo.aiimage;

/*                AiImageResponseVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image response vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Image generation response: the artifact in the requested form (url / base64).
 * When the provider returned raw bytes they are exposed as base64.
 */

@Data
public class AiImageResponseVo011 {

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
    @Schema(description = "媒体类型（如 image/png）")
    private String mimeType;

    /** Storage instance code used for the write. */
    @Schema(description = "存储实例 code（落盘所用；业务可自记，见存储中心 016 规约）")
    private String storageCode;

    /** Physical bucket name used for the write. */
    @Schema(description = "物理桶名（落盘所用；业务可自记）")
    private String bucket;

    /** Storage object key of the persisted artifact. */
    @Schema(description = "对象键 objectKey（落盘后，生命周期归使用方；可自记）")
    private String storageKey;
}

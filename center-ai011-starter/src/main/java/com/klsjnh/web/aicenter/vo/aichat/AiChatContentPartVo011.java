package com.klsjnh.web.aicenter.vo.aichat;

/*                AiChatContentPartVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  multimodal content part vo (text / image_url)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * One typed content part in a chat request: text or an image reference. The
 * framework accepts a base64 data URL (png / jpeg / jpg / webp) or an https URL
 * and never downloads the image.
 */

@Data
public class AiChatContentPartVo011 {

    /** Part type: text / image_url. */
    @NotBlank(message = "type is required")
    @Schema(description = "内容块类型（text / image_url）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    /** Text value, for type=text. */
    @Schema(description = "文本内容（type=text 时必填）")
    private String text;

    /** Image URL, for type=image_url. */
    @Schema(description = "图片地址（type=image_url 时必填；data:image/(png|jpeg|jpg|webp);base64, 或 https://）")
    private String imageUrl;
}

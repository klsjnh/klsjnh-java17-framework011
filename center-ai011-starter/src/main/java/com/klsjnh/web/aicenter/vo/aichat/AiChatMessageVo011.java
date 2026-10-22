package com.klsjnh.web.aicenter.vo.aichat;

/*                AiChatMessageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate 2026.10.22
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat message request vo 011 class
 *      2026.10.22  contentParts for multimodal messages (content kept for text)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * One chat message in the request: plain text via {@code content}, or a
 * multimodal block list via {@code contentParts}. The two are mutually
 * exclusive.
 */

@Data
public class AiChatMessageVo011 {

    /** Role: system / user / assistant. */
    @Schema(description = "角色（system / user / assistant）")
    private String role;

    /** Message text (text-only form). */
    @Schema(description = "消息内容（纯文本；与 contentParts 二选一）")
    private String content;

    /** Typed content parts (multimodal form). */
    @Schema(description = "多模态内容块（text / image_url；与 content 二选一）")
    private List<AiChatContentPartVo011> contentParts;
}

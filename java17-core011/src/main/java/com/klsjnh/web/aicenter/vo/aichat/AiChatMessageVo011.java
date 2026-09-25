package com.klsjnh.web.aicenter.vo.aichat;

/*                AiChatMessageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat message request vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One chat message in the request.
 */

@Data
public class AiChatMessageVo011 {

    /** Role: system / user / assistant. */
    @Schema(description = "角色（system / user / assistant）")
    private String role;

    /** Message text. */
    @Schema(description = "消息内容")
    private String content;
}

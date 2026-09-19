package com.klsjnh.web.messagecenter.vo.message;

/*                JulyMessageSendResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message send result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Send result response VO: the persisted record id plus the channel outcome.
 */

@Data
public class JulyMessageSendResultVo011 {

    /** Send record id. */
    @Schema(description = "发送记录 id")
    private String messageId;

    /** Whether the send succeeded. */
    @Schema(description = "是否成功")
    private boolean success;

    /** Channel-side message id. */
    @Schema(description = "渠道侧消息 id")
    private String channelMessageId;

    /** Failure reason. */
    @Schema(description = "失败原因")
    private String error;
}

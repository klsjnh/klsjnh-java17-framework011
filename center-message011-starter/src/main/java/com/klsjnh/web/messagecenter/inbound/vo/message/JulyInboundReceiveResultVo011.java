package com.klsjnh.web.messagecenter.inbound.vo.message;

/*                JulyInboundReceiveResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound receive result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Receive result response VO: the persisted record id, the duplicate flag and
 * the optional encoded response body.
 */

@Data
public class JulyInboundReceiveResultVo011 {

    /** Received record id. */
    @Schema(description = "接收记录 id")
    private String messageId;

    /** Whether the message was already received. */
    @Schema(description = "是否重复消息")
    private boolean duplicate;

    /** Encoded response body, optional. */
    @Schema(description = "渠道编码后的响应体（无回复时为空）")
    private String responseBody;
}

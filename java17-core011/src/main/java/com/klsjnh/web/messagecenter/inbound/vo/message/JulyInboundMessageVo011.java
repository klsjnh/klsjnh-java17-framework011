package com.klsjnh.web.messagecenter.inbound.vo.message;

/*                JulyInboundMessageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Received record response VO (detail and page rows).
 */

@Data
public class JulyInboundMessageVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Channel code. */
    @Schema(description = "渠道编码")
    private String channelCode;

    /** Provider type that parsed the message. */
    @Schema(description = "提供商类型")
    private String providerType;

    /** Channel-defined message shape. */
    @Schema(description = "报文形态（渠道自定义）")
    private String messageType;

    /** Channel-defined payload JSON. */
    @Schema(description = "形态载荷 JSON（渠道自定义）")
    private String payload;

    /** Sender. */
    @Schema(description = "发送方")
    private String fromId;

    /** Parsed content. */
    @Schema(description = "内容")
    private String content;

    /** Channel-side message id. */
    @Schema(description = "渠道侧消息 id")
    private String rawMessageId;

    /** Handling failure reason. */
    @Schema(description = "处理失败原因")
    private String error;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Handling status (MessageInboundStatus011 code). */
    @Schema(description = "处理状态（1 已接收 / 2 已处理 / 3 处理失败）")
    private String status;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}

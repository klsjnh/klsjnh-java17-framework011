package com.klsjnh.web.messagecenter.vo.message;

/*                JulyMessageSendVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message send vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Send request VO: a channel, a receiver, an optional template plus variables,
 * or a raw title / content.
 */

@Data
public class JulyMessageSendVo011 {

    /** Channel code. */
    @Schema(description = "渠道编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    /** Receiver, optional. */
    @Schema(description = "接收方（用户 id / 手机号 / webhook 标识）")
    private String to;

    /** Template code, optional. */
    @Schema(description = "模板编码（可空，纯文本直发）")
    private String templateCode;

    /** Template variables, optional. */
    @Schema(description = "模板变量（${var} 占位替换）")
    private Map<String, String> params;

    /** Raw title, optional. */
    @Schema(description = "标题（不传模板时直发）")
    private String title;

    /** Raw content, optional. */
    @Schema(description = "内容（不传模板时直发）")
    private String content;

    /** Remark, optional. */
    @Schema(description = "备注")
    private String remark;
}

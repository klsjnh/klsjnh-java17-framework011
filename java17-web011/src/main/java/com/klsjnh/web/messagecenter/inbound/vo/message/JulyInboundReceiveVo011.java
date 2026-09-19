package com.klsjnh.web.messagecenter.inbound.vo.message;

/*                JulyInboundReceiveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound receive vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Receive request VO carrying the raw request body handed to the inbound
 * channel port; the channel code travels as a query parameter.
 */

@Data
public class JulyInboundReceiveVo011 {

    /** Raw request body, optional. */
    @Schema(description = "原始请求体（由入站渠道端口解析）")
    private String rawBody;
}

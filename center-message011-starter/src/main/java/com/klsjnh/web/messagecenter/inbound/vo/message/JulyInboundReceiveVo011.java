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

import jakarta.validation.constraints.Size;

/**
 * Receive request VO carrying the raw request body handed to the inbound
 * channel port; the channel code travels as a query parameter.
 */

@Data
public class JulyInboundReceiveVo011 {

    /** Raw request body, optional, capped at 1M characters (DoS guard). */
    @Size(max = 1_000_000, message = "rawBody exceeds the 1,000,000 character limit")
    @Schema(description = "原始请求体（由入站渠道端口解析，最长 1,000,000 字符）")
    private String rawBody;
}

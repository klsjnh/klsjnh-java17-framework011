package com.klsjnh.web.messagecenter.inbound.vo.message;

/*                JulyInboundMessageQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for received records (channel + status + keyword).
 */

@Data
public class JulyInboundMessageQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Exact channel code filter, blank for all. */
    @Schema(description = "渠道编码过滤（留空为全部）")
    private String channelCode;

    /** Exact handling status filter (MessageInboundStatus011 code), blank for all. */
    @Schema(description = "处理状态过滤（1 已接收 / 2 已处理 / 3 处理失败，留空为全部）")
    private String status;

    /** Keyword matched against sender / content / raw message id. */
    @Schema(description = "关键字（发送方 / 内容 / 渠道消息 id 模糊）")
    private String keyword;
}

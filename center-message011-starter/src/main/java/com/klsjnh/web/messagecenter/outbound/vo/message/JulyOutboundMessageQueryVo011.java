package com.klsjnh.web.messagecenter.outbound.vo.message;

/*                JulyOutboundMessageQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for send records (channel + status + keyword).
 */

@Data
public class JulyOutboundMessageQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Exact channel code filter, blank for all. */
    @Schema(description = "渠道编码过滤（留空为全部）")
    private String channelCode;

    /** Exact send status filter (MessageStatus011 code), blank for all. */
    @Schema(description = "发送状态过滤（1 待发 / 2 成功 / 3 失败，留空为全部）")
    private String status;

    /** Keyword matched against receiver / title / template code. */
    @Schema(description = "关键字（接收方 / 标题 / 模板编码 模糊）")
    private String keyword;
}

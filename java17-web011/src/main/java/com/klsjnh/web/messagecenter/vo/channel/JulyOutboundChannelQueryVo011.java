package com.klsjnh.web.messagecenter.vo.channel;

/*                JulyOutboundChannelQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for message channels (keyword + optional status).
 */

@Data
public class JulyOutboundChannelQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Keyword matched against code / name / provider type. */
    @Schema(description = "关键字（编码 / 名称 / 提供商类型 模糊）")
    private String keyword;

    /** Row status filter, blank for all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用，留空为全部）")
    private String status;
}

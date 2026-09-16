package com.klsjnh.web.ai011.vo.aimodelprovider;

/*                AiModelProviderQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for providers (keyword + optional status).
 */

@Data
public class AiModelProviderQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Keyword matched against code / name / base url. */
    @Schema(description = "关键字（编码 / 名称 / Base URL 模糊）")
    private String keyword;

    /** Row status filter, blank for all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用，留空为全部）")
    private String status;
}

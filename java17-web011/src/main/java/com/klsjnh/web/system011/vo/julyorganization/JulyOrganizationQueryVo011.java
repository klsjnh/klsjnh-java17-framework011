package com.klsjnh.web.system011.vo.julyorganization;

/*                JulyOrganizationQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for organizations.
 */

@Data
public class JulyOrganizationQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** Org code / name keyword (fuzzy). */
    @Schema(description = "组织编码 / 名称关键字（模糊）")
    private String keyword;
}

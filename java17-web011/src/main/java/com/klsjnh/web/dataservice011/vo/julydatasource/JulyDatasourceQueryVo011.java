package com.klsjnh.web.dataservice011.vo.julydatasource;

/*                JulyDatasourceQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for datasource entries.
 */

@Data
public class JulyDatasourceQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** dsCode / dsName / jdbcUrl keyword (fuzzy). */
    @Schema(description = "编码 / 名称 / JDBC URL 关键字（模糊）")
    private String keyword;

    /** Row status filter: 0 disabled / 1 enabled; blank means all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用；留空为全部）")
    private String status;
}

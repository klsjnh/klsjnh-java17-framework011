package com.klsjnh.web.lowcode011.vo.julybusinessmodeling;

/*                JulyBusinessModelingPageExecuteVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling page execute vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Paged execute request VO: run a read-only SQL with dialect paging.
 */

@Data
public class JulyBusinessModelingPageExecuteVo011 {

    /** Datasource id selector; at most one of id / code. */
    @Schema(description = "数据源 id（与 dataSourceCode 二选一；均不传则取建模的数据源）")
    private String dataSourceId;

    /** Datasource code selector; at most one of id / code. */
    @Schema(description = "数据源编码（与 dataSourceId 二选一；均不传则取建模的数据源）")
    private String dataSourceCode;

    /** Modeling id selector (SQL source). */
    @Schema(description = "建模 id（与 modelCode / sqlContent 三选一，取其 SQL）")
    private String modelId;

    /** Modeling code selector (SQL source). */
    @Schema(description = "建模编码（与 modelId / sqlContent 三选一，取其 SQL）")
    private String modelCode;

    /** Raw read-only SQL (SQL source). */
    @Schema(description = "只读 SELECT 语句（与 modelId / modelCode 三选一）")
    private String sqlContent;

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size, clamped by the routing port to [10, 500]. */
    @Schema(description = "每页条数（后端 clamp 到 [10, 500]）")
    private Integer pageSize;
}

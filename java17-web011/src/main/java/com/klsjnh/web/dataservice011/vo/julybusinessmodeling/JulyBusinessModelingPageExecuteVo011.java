package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

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

    /** Datasource code, must be enabled. */
    @Schema(description = "数据源编码（须存在且启用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataSourceCode;

    /** Read-only SQL. */
    @Schema(description = "只读 SELECT 语句", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sqlContent;

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size, clamped by the routing port to [10, 500]. */
    @Schema(description = "每页条数（后端 clamp 到 [10, 500]）")
    private Integer pageSize;
}

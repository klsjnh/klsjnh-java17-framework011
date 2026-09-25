package com.klsjnh.web.datasource.vo.julysql;

/*                SqlQueryPageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sql page query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Read-only SQL paged query request: a dynamic datasource, a SELECT with
 * {@code ?} placeholders, the bound parameters and the page.
 */

@Data
public class SqlQueryPageVo011 {

    /** Dynamic datasource code. */
    @Schema(description = "数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dsCode;

    /** Select statement with {@code ?} placeholders (read-only). */
    @Schema(description = "SELECT 语句（? 占位，只读）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sql;

    /** Bound parameters, nullable for none. */
    @Schema(description = "绑定参数（与 ? 一一对应）")
    private List<Object> params;

    /** Page index, starts at 1. */
    @Schema(description = "页码（从 1 起）")
    private Integer pageIndex;

    /** Page size, clamped to [10, 500]. */
    @Schema(description = "每页条数（夹取 [10,500]）")
    private Integer pageSize;
}

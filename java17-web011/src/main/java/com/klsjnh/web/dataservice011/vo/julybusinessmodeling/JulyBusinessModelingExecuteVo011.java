package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

/*                JulyBusinessModelingExecuteVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling execute vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Execute request VO: run a read-only SQL against a business datasource.
 */

@Data
public class JulyBusinessModelingExecuteVo011 {

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
}

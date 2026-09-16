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

    /** Datasource code, must be enabled. */
    @Schema(description = "数据源编码（须存在且启用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataSourceCode;

    /** Read-only SQL. */
    @Schema(description = "只读 SELECT 语句", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sqlContent;
}

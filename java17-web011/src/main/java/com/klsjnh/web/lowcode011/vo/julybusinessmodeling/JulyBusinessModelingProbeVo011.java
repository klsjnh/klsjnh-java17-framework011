package com.klsjnh.web.lowcode011.vo.julybusinessmodeling;

/*                JulyBusinessModelingProbeVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling probe vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Probe request VO: run a draft SQL and infer the fields (nothing persisted).
 */

@Data
public class JulyBusinessModelingProbeVo011 {

    /** Datasource code, must be enabled. */
    @Schema(description = "数据源编码（须存在且启用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataSourceCode;

    /** Read-only SQL. */
    @Schema(description = "只读 SELECT 语句", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sqlContent;

    /** Object name, informational. */
    @Schema(description = "低代码对象名（仅记录）")
    private String objectName;
}

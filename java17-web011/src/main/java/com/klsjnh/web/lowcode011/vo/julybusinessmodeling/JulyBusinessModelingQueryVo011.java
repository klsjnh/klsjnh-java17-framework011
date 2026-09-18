package com.klsjnh.web.lowcode011.vo.julybusinessmodeling;

/*                JulyBusinessModelingQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for modeling records.
 */

@Data
public class JulyBusinessModelingQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Keyword matched against modelCode / modelName / objectName. */
    @Schema(description = "关键字（编码 / 名称 / 对象名 模糊）")
    private String keyword;

    /** Exact datasource code filter. */
    @Schema(description = "数据源编码过滤")
    private String dataSourceCode;

    /** Row status filter, blank for all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用，留空为全部）")
    private String status;
}

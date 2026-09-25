package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sync rule page query request.
 */

@Data
public class JulySyncQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码（从 1 起）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Sync code / name keyword. */
    @Schema(description = "编码/名称关键字")
    private String keyword;

    /** Status filter. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;
}

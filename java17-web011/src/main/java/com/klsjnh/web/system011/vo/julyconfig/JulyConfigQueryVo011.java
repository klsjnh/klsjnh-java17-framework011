package com.klsjnh.web.system011.vo.julyconfig;

/*                JulyConfigQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for config entries.
 */

@Data
public class JulyConfigQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** Code / data keyword (fuzzy). */
    @Schema(description = "配置项 / 配置值关键字（模糊）")
    private String keyword;
}

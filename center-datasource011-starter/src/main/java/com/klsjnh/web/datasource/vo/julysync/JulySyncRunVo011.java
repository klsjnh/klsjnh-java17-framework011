package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncRunVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync run vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sync run request: which rule to run.
 */

@Data
public class JulySyncRunVo011 {

    /** Sync code. */
    @Schema(description = "同步编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncCode;
}

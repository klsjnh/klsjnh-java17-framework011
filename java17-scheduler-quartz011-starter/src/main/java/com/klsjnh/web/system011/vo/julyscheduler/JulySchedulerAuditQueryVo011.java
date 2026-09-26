package com.klsjnh.web.system011.vo.julyscheduler;

/*                JulySchedulerAuditQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Page query request VO for scheduler execution-audit rows (child table).
 */

@Data
public class JulySchedulerAuditQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** Master link: july_scheduler.id (pk_mt), required. */
    @NotBlank(message = "pkMt is required")
    @Schema(description = "主表链接（july_scheduler.id / pk_mt，必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pkMt;
}

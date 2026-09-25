package com.klsjnh.web.iam.vo.julyuseraudit;

/*                JulyUserAuditQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for the user audit trail.
 */

@Data
public class JulyUserAuditQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** Operator account keyword (fuzzy). */
    @Schema(description = "操作者账号关键字（模糊）")
    private String userAccount;

    /** Exact event type, e.g. LOGIN / LOGIN_FAILED / CHANGE_PASSWORD. */
    @Schema(description = "事件类型（精确，如 LOGIN / LOGIN_FAILED / CHANGE_PASSWORD）")
    private String auditType;

    /** Event time lower bound (inclusive). */
    @Schema(description = "事件时间下界（含）")
    private String beginTime;

    /** Event time upper bound (inclusive). */
    @Schema(description = "事件时间上界（含）")
    private String endTime;
}

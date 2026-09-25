package com.klsjnh.web.system011.vo.julyscheduler;

/*                JulySchedulerAuditVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Scheduler execution-audit response VO (one run outcome).
 */

@Data
public class JulySchedulerAuditVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Master link: july_scheduler.id (pk_mt). */
    @Schema(description = "主表链接（july_scheduler.id / pk_mt）")
    private String pkMt;

    /** Task code snapshot. */
    @Schema(description = "任务编码快照")
    private String schedulerCode;

    /** Run start time. */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /** Run end time. */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /** Execution result: SUCCESS / FAIL. */
    @Schema(description = "执行结果（SUCCESS/FAIL）")
    private String execStatus;

    /** Truncated failure message. */
    @Schema(description = "失败信息（截断）")
    private String errorMessage;

    /** Row create time (≈ event time). */
    @Schema(description = "创建时间（≈事件时间）")
    private LocalDateTime createTime;
}

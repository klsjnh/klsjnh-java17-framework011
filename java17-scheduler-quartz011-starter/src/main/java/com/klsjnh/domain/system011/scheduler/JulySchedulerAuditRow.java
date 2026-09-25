package com.klsjnh.domain.system011.scheduler;

/*                JulySchedulerAuditRow record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit row record
 *
 */

import java.time.LocalDateTime;

/**
 * Execution-audit read model: one append-only run outcome for a scheduler
 * definition (child of {@code july_scheduler}).
 *
 * @param id             primary key
 * @param pkMt           master task id ({@code july_scheduler.id} via {@code pk_mt})
 * @param schedulerCode  task code snapshot, nullable
 * @param startTime      run start
 * @param endTime        run end, nullable when incomplete
 * @param execStatus     SUCCESS / FAIL
 * @param errorMessage   truncated failure hint, nullable
 * @param createTime     row create time (≈ event time)
 */

public record JulySchedulerAuditRow(String id, String pkMt, String schedulerCode, LocalDateTime startTime,
        LocalDateTime endTime, String execStatus, String errorMessage, LocalDateTime createTime) {
}

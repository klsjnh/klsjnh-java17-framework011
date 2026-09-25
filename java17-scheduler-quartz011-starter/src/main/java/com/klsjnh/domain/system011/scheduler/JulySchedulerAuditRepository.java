package com.klsjnh.domain.system011.scheduler;

/*                JulySchedulerAuditRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit repository interface
 *
 */

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository port for {@code july_scheduler_audit}: append-only write of run
 * outcomes, plus read by master scheduler id ({@code pk_mt}). Start / stop
 * management actions do not write here — they go to {@code july_user_audit}
 * via {@code @AuditLog}.
 */

public interface JulySchedulerAuditRepository {

    /**
     * Insert one execution-audit row (append-only).
     *
     * @param pkMt          master task id ({@code pk_mt})
     * @param schedulerCode task code snapshot, nullable
     * @param startTime     run start
     * @param endTime       run end
     * @param execStatus    SUCCESS / FAIL persistence code
     * @param errorMessage  truncated failure hint, nullable
     */
    void insert(String pkMt, String schedulerCode, LocalDateTime startTime, LocalDateTime endTime,
            String execStatus, String errorMessage);

    /**
     * Offset-based page of executions for one scheduler, newest first.
     *
     * @param offset  zero-based row offset
     * @param pageSize page size
     * @param pkMt    master task id ({@code pk_mt})
     * @return page rows
     */
    List<JulySchedulerAuditRow> findPageByScheduler(int offset, int pageSize, String pkMt);

    /**
     * Count executions for one scheduler.
     *
     * @param pkMt master task id ({@code pk_mt})
     * @return total row count
     */
    long countByScheduler(String pkMt);
}

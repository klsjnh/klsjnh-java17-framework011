package com.klsjnh.infrastructure.system011.scheduler;

/*                SchedulerExecAuditRecorder class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  scheduler exec audit recorder class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRepository;
import com.klsjnh.domain.system011.scheduler.SchedulerExecStatus011;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Writes july_scheduler_audit rows (append-only run outcomes). Failures are
 * logged and never interrupt the Quartz schedule.
 * <p>
 * {@code REQUIRES_NEW} mirrors {@code UserAuditRecorder}: the exec-audit row
 * must survive a rollback of an adjacent business update (e.g. execute_times).
 * </p>
 */

@Component
public class SchedulerExecAuditRecorder {

    /**
     * Max length for {@code error_message} (matches DDL VARCHAR(500)).
     */
    public static final int ERROR_MESSAGE_MAX = 500;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SchedulerExecAuditRecorder.class);

    /**
     * Audit repository.
     */
    private final JulySchedulerAuditRepository auditRepository;

    /**
     * Create the recorder.
     *
     * @param auditRepository july scheduler audit repository
     */
    public SchedulerExecAuditRecorder(JulySchedulerAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Insert one run-outcome row; failures are logged and swallowed.
     *
     * @param pkMt          master task id ({@code pk_mt})
     * @param schedulerCode task code snapshot, nullable
     * @param startTime     run start
     * @param endTime       run end
     * @param execStatus    SUCCESS / FAIL
     * @param errorMessage  failure hint, truncated to 500 chars, nullable
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String pkMt, String schedulerCode, LocalDateTime startTime, LocalDateTime endTime,
            SchedulerExecStatus011 execStatus, String errorMessage) {
        String funcName = "scheduler exec audit write";

        try {
            auditRepository.insert(pkMt, schedulerCode, startTime, endTime, execStatus.getCode(),
                    truncate(errorMessage));
        } catch (Exception ex) {
            logger.warn("{} pk={} status={} failed {} ...", funcName, pkMt, execStatus.getCode(),
                    ex.getMessage());
        }
    }

    /**
     * Truncate error text to the DDL column length.
     *
     * @param message raw message, nullable
     * @return truncated text, or null
     */
    static String truncate(String message) {
        if (message == null) {
            return null;
        }

        if (message.length() <= ERROR_MESSAGE_MAX) {
            return message;
        }

        return message.substring(0, ERROR_MESSAGE_MAX);
    }
}

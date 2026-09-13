package com.klsjnh.domain.system011.scheduler;

/*                JulyScheduler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyScheduler aggregate root (system management context): a scheduled task
 * definition driven by a cron expression.
 * <p>
 * Runtime status reuses the common status column values — transitions go
 * through {@link Status011} (the shared-kernel enum from common). New tasks
 * default to stopped; scheduling requires the explicit start operation.
 * </p>
 */

public class JulyScheduler {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Scheduler code, unique, immutable after create.
     */
    private final String schedulerCode;

    /**
     * Scheduler name.
     */
    private String schedulerName;

    /**
     * Handler content: Spring bean name implementing Runnable.
     */
    private String schedulerHandler;

    /**
     * Cron expression.
     */
    private String schedulerCron;

    /**
     * Execute times, incremented on every trigger.
     */
    private Integer executeTimes;

    /**
     * Runtime status: '1' running / '0' stopped.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id               primary key
     * @param schedulerCode    scheduler code, unique
     * @param schedulerName    scheduler name
     * @param schedulerHandler handler content (Spring bean name)
     * @param schedulerCron    cron expression
     * @param executeTimes     execute times, null falls back to 0
     * @param status           runtime status, null falls back to stopped
     * @param audit            audit info
     */
    public JulyScheduler(EntityId id, String schedulerCode, String schedulerName, String schedulerHandler,
            String schedulerCron, Integer executeTimes, String status, AuditInfo audit) {
        this.id = id;
        this.schedulerCode = schedulerCode;
        this.schedulerName = schedulerName;
        this.schedulerHandler = schedulerHandler;
        this.schedulerCron = schedulerCron;
        this.executeTimes = executeTimes == null ? 0 : executeTimes;
        this.status = status == null ? Status011.DISABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new task: defaults to stopped with zero executions.
     *
     * @param id               primary key
     * @param schedulerCode    scheduler code, unique, max 30
     * @param schedulerName    scheduler name, max 60
     * @param schedulerHandler handler content, max 300
     * @param schedulerCron    cron expression, max 30
     * @param audit            audit info
     * @return new aggregate in stopped state
     */
    public static JulyScheduler create(EntityId id, String schedulerCode, String schedulerName,
            String schedulerHandler, String schedulerCron, AuditInfo audit) {
        validateBasics(schedulerCode, schedulerName, schedulerHandler, schedulerCron);

        return new JulyScheduler(id, schedulerCode, schedulerName, schedulerHandler, schedulerCron, 0,
                Status011.DISABLED.getCode(), audit);
    }

    /**
     * Update the mutable basics; the code is immutable after create.
     *
     * @param schedulerName    scheduler name, max 60
     * @param schedulerHandler handler content, max 300
     * @param schedulerCron    cron expression, max 30
     */
    public void updateBasics(String schedulerName, String schedulerHandler, String schedulerCron) {
        validateBasics(this.schedulerCode, schedulerName, schedulerHandler, schedulerCron);
        this.schedulerName = schedulerName;
        this.schedulerHandler = schedulerHandler;
        this.schedulerCron = schedulerCron;
    }

    /**
     * Switch to running.
     */
    public void start() {
        this.status = Status011.ENABLED.getCode();
    }

    /**
     * Switch to stopped.
     */
    public void stop() {
        this.status = Status011.DISABLED.getCode();
    }

    /**
     * Validate the mutable basics.
     *
     * @param code    scheduler code
     * @param name    scheduler name
     * @param handler handler content
     * @param cron    cron expression
     */
    private static void validateBasics(String code, String name, String handler, String cron) {
        if (StringUtil011.isMissing(code, 30)) {
            throw new IllegalArgumentException("scheduler code is required (max 30)");
        }

        if (StringUtil011.isMissing(name, 60)) {
            throw new IllegalArgumentException("scheduler name is required (max 60)");
        }

        if (StringUtil011.isMissing(handler, 300)) {
            throw new IllegalArgumentException("scheduler handler is required (max 300)");
        }

        if (StringUtil011.isMissing(cron, 30)) {
            throw new IllegalArgumentException("scheduler cron is required (max 30)");
        }
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the scheduler code.
     *
     * @return scheduler code
     */
    public String schedulerCode() {
        return schedulerCode;
    }

    /**
     * Get the scheduler name.
     *
     * @return scheduler name
     */
    public String schedulerName() {
        return schedulerName;
    }

    /**
     * Get the handler content.
     *
     * @return handler content (Spring bean name)
     */
    public String schedulerHandler() {
        return schedulerHandler;
    }

    /**
     * Get the cron expression.
     *
     * @return cron expression
     */
    public String schedulerCron() {
        return schedulerCron;
    }

    /**
     * Get the execute times.
     *
     * @return execute times
     */
    public Integer executeTimes() {
        return executeTimes;
    }

    /**
     * Get the runtime status.
     *
     * @return '1' running / '0' stopped
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}

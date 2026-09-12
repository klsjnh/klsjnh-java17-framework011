package com.klsjnh.domain.scheduler;

/*                SchedulerPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  scheduler port interface
 *
 */

/**
 * Scheduler engine port: the Quartz adapter lives in infrastructure. All
 * operations are idempotent — registering an existing id replaces it,
 * removing a missing id is ignored.
 */

public interface SchedulerPort {

    /**
     * Register (or replace) a running task driven by the cron expression.
     *
     * @param id      task id
     * @param handler handler content (Spring bean name implementing Runnable)
     * @param cron    cron expression
     */
    void register(String id, String handler, String cron);

    /**
     * Remove a task from the engine; missing jobs are ignored.
     *
     * @param id task id
     */
    void remove(String id);

    /**
     * Trigger the handler once immediately, regardless of the runtime status.
     *
     * @param id      task id
     * @param handler handler content (Spring bean name implementing Runnable)
     */
    void triggerOnce(String id, String handler);

    /**
     * Validate a cron expression.
     *
     * @param cron cron expression
     * @return true when the expression can be parsed
     */
    boolean validateCron(String cron);
}

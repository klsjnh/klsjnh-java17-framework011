package com.klsjnh.domain.system011.scheduler;

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
     * @param id        task id
     * @param handler   handler name (JobHandler handlerName)
     * @param cron      cron expression
     * @param taskParam task payload (JSON text) handed to the handler, nullable
     */
    void register(String id, String handler, String cron, String taskParam);

    /**
     * Remove a task from the engine; missing jobs are ignored.
     *
     * @param id task id
     */
    void remove(String id);

    /**
     * Trigger the handler once immediately, regardless of the runtime status.
     *
     * @param id        task id
     * @param handler   handler name (JobHandler handlerName)
     * @param taskParam task payload (JSON text) handed to the handler, nullable
     */
    void triggerOnce(String id, String handler, String taskParam);

    /**
     * Validate a cron expression.
     *
     * @param cron cron expression
     * @return true when the expression can be parsed
     */
    boolean validateCron(String cron);
}

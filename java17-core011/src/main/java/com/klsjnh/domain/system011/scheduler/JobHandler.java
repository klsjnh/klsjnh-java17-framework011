package com.klsjnh.domain.system011.scheduler;

/*                JobHandler interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  job handler interface
 *
 */

import java.util.Map;

/**
 * Task handler contract: implemented as Spring beans and resolved by name —
 * the {@code scheduler_handler} column stores {@link #handlerName()}.
 */

public interface JobHandler {

    /**
     * Get the unique handler name used in july_scheduler.scheduler_handler.
     *
     * @return handler name
     */
    String handlerName();

    /**
     * Execute the task.
     *
     * @param payload job payload from the scheduler, may be empty
     */
    void execute(Map<String, Object> payload);
}

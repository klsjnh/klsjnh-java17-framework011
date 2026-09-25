package com.klsjnh.domain.system011.scheduler;

/*                JobHandlerRegistryPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  job handler registry port interface
 *
 */

import java.util.List;

/**
 * Registry port for the JobHandler beans of the context: the scheduler engine
 * resolves {@code scheduler_handler} values through it.
 */

public interface JobHandlerRegistryPort {

    /**
     * Whether a handler with the given name is registered.
     *
     * @param handlerName handler name
     * @return true when registered
     */
    boolean exists(String handlerName);

    /**
     * All registered handler names.
     *
     * @return handler name list
     */
    List<String> names();
}

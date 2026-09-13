package com.klsjnh.infrastructure.scheduler;

/*                JobHandlerRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  job handler registry class
 *
 */

import com.klsjnh.domain.scheduler.JobHandler;
import com.klsjnh.domain.scheduler.JobHandlerRegistryPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link JobHandler} bean of the context into a name → handler
 * map (immutable snapshot at startup).
 */

@Component
public class JobHandlerRegistry implements JobHandlerRegistryPort {

    /**
     * Handler name to bean mapping.
     */
    private final Map<String, JobHandler> handlers;

    /**
     * Create the registry from all JobHandler beans.
     *
     * @param handlerBeans handler beans found in the context
     */
    public JobHandlerRegistry(List<JobHandler> handlerBeans) {
        this.handlers = handlerBeans.stream()
                .collect(Collectors.toUnmodifiableMap(JobHandler::handlerName, Function.identity()));
    }

    /**
     * Resolve a handler by name.
     *
     * @param handlerName handler name
     * @return handler or null when unknown
     */
    public JobHandler get(String handlerName) {
        return handlers.get(handlerName);
    }

    /**
     * Whether a handler with the given name is registered.
     *
     * @param handlerName handler name
     * @return true when registered
     */
    @Override
    public boolean exists(String handlerName) {
        return handlers.containsKey(handlerName);
    }

    /**
     * All registered handler names.
     *
     * @return handler name list
     */
    @Override
    public List<String> names() {
        return List.copyOf(handlers.keySet());
    }
}

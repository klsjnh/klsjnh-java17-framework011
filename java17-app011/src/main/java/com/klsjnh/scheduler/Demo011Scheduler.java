package com.klsjnh.scheduler;

/*                Demo011Scheduler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 scheduler class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.scheduler.JobHandler;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Demo handler for the julyScheduler engine, written the framework way:
 * implement {@link JobHandler} as a Spring bean — the
 * {@code scheduler_handler} column stores {@link #handlerName()}
 * ("demo011scheduler") and the engine resolves it through the
 * JobHandlerRegistry.
 * <p>
 * Seed task: scheduler_code = demo011, cron {@code 0/30 * * * * ?} (every 30
 * seconds). Register it via the julyScheduler insert + start endpoints; a
 * missing / failing handler never interrupts the schedule (WARN + counter).
 * </p>
 */

@Component
public class Demo011Scheduler implements JobHandler {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Demo011Scheduler.class);

    /**
     * Get the unique handler name used in july_scheduler.scheduler_handler.
     *
     * @return handler name
     */
    @Override
    public String handlerName() {
        return "demo011scheduler";
    }

    /**
     * Execute the task: one demo tick.
     *
     * @param payload job payload from the scheduler, may be empty
     */
    @Override
    public void execute(Map<String, Object> payload) {
        String funcName = "demo tick";

        logger.info("{} {} {} ...", funcName, "julyScheduler demo011", payload);
    }
}

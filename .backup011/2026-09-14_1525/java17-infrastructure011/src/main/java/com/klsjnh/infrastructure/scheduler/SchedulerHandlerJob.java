package com.klsjnh.infrastructure.scheduler;

/*                SchedulerHandlerJob class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  scheduler handler job class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.system011.scheduler.JobHandler;
import com.klsjnh.infrastructure.system011.entity.JulySchedulerPo;
import com.klsjnh.infrastructure.system011.mapper.JulySchedulerMapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Quartz job: invokes the handler bean and increments the execute counter.
 * <p>
 * Counted on every trigger, success or failure (requirement011 §023-2). A
 * missing or failing handler bean logs a WARN and never interrupts the
 * schedule.
 * </p>
 */

public class SchedulerHandlerJob implements Job {

    /**
     * Job data key: task id.
     */
    public static final String DATA_ID = "id";

    /**
     * Job data key: handler content.
     */
    public static final String DATA_HANDLER = "handler";

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SchedulerHandlerJob.class);

    /**
     * Handler registry (resolves scheduler_handler by name).
     */
    @Autowired
    private JobHandlerRegistry handlerRegistry;

    /**
     * Mapper for the execute counter increment.
     */
    @Autowired
    private JulySchedulerMapper mapper;

    /**
     * Run the handler and count the trigger. The handler resolves through the
     * JobHandlerRegistry; missing or failing handlers log a WARN and never
     * interrupt the schedule.
     *
     * @param context quartz execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        String funcName = "scheduler job";

        JobDataMap data = context.getMergedJobDataMap();
        String id = data.getString(DATA_ID);
        String handler = data.getString(DATA_HANDLER);

        try {
            JobHandler jobHandler = handlerRegistry.get(handler);

            if (jobHandler == null) {
                logger.warn("{} {} handler {} not registered ...", funcName, id, handler);
            } else {
                jobHandler.execute(data);
            }
        } catch (Exception ex) {
            logger.warn("{} {} {} failed {} ...", funcName, id, handler, ex.getMessage());
        } finally {
            incrementTimes(id);
        }
    }

    /**
     * Increment the execute counter of a task (atomic SQL increment).
     *
     * @param id task id
     */
    private void incrementTimes(String id) {
        LambdaUpdateWrapper<JulySchedulerPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("execute_times = execute_times + 1").eq(JulySchedulerPo::getId, id);
        mapper.update(null, wrapper);
    }
}

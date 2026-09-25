package com.klsjnh.infrastructure.system011.scheduler;

/*                SchedulerHandlerJob class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  scheduler handler job class
 *      2026.09.22  field @Autowired kept — Quartz JobFactory exception (017)
 *      2026.09.26  write july_scheduler_audit run outcome on each fire
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.system011.scheduler.JobHandler;
import com.klsjnh.domain.system011.scheduler.SchedulerExecStatus011;

import com.klsjnh.infrastructure.system011.scheduler.entity.JulySchedulerPo;
import com.klsjnh.infrastructure.system011.scheduler.mapper.JulySchedulerMapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;

/**
 * Quartz job: invokes the handler bean, increments the execute counter, and
 * appends one july_scheduler_audit row for the run outcome.
 * <p>
 * Counted on every trigger, success or failure (requirement011 §023-2). A
 * missing or failing handler bean logs a WARN, records FAIL, and never
 * interrupts the schedule.
 * </p>
 * <p>
 * Field {@code @Autowired} is required here: Spring Boot's default
 * {@code AutowiringSpringBeanJobFactory} constructs the job with a no-arg
 * constructor then injects beans. Constructor injection would break Quartz
 * scheduling (explicit redline exception — docs/017 §011.016).
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
     * Mapper for the execute counter increment and code snapshot.
     */
    @Autowired
    private JulySchedulerMapper mapper;

    /**
     * Append-only execution-audit writer.
     */
    @Autowired
    private SchedulerExecAuditRecorder execAuditRecorder;

    /**
     * Run the handler, count the trigger, and record the run outcome.
     *
     * @param context quartz execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        String funcName = "scheduler job";

        JobDataMap data = context.getMergedJobDataMap();
        String id = data.getString(DATA_ID);
        String handler = data.getString(DATA_HANDLER);

        LocalDateTime startTime = LocalDateTime.now();
        SchedulerExecStatus011 execStatus = SchedulerExecStatus011.SUCCESS;
        String errorMessage = null;

        try {
            JobHandler jobHandler = handlerRegistry.get(handler);

            if (jobHandler == null) {
                execStatus = SchedulerExecStatus011.FAIL;
                errorMessage = "handler not registered: " + handler;
                logger.warn("{} {} handler {} not registered ...", funcName, id, handler);
            } else {
                jobHandler.execute(data);
            }
        } catch (Exception ex) {
            execStatus = SchedulerExecStatus011.FAIL;
            errorMessage = ex.getMessage();
            logger.warn("{} {} {} failed {} ...", funcName, id, handler, ex.getMessage());
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            incrementTimes(id);
            recordExecAudit(id, startTime, endTime, execStatus, errorMessage);
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

    /**
     * Append one execution-audit row; failures are swallowed inside the
     * recorder.
     *
     * @param id           task id
     * @param startTime    run start
     * @param endTime      run end
     * @param execStatus   SUCCESS / FAIL
     * @param errorMessage failure hint, nullable
     */
    private void recordExecAudit(String id, LocalDateTime startTime, LocalDateTime endTime,
            SchedulerExecStatus011 execStatus, String errorMessage) {
        String schedulerCode = null;

        try {
            JulySchedulerPo po = mapper.selectById(id);

            if (po != null) {
                schedulerCode = po.getSchedulerCode();
            }
        } catch (Exception ex) {
            logger.warn("scheduler job {} code snapshot failed {} ...", id, ex.getMessage());
        }

        execAuditRecorder.record(id, schedulerCode, startTime, endTime, execStatus, errorMessage);
    }
}

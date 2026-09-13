package com.klsjnh.infrastructure.scheduler;

/*                SchedulerEngine class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  scheduler engine class
 *
 */

import com.klsjnh.domain.system011.scheduler.SchedulerPort;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Quartz-backed implementation of SchedulerPort (RAMJobStore).
 * <p>
 * Job identity: {@code scheduler_<id>} in group {@code julyScheduler}. All
 * operations are idempotent; register replaces an existing job.
 * </p>
 */

@Component
public class SchedulerEngine implements SchedulerPort {

    /**
     * Job / trigger group for all scheduler tasks.
     */
    private static final String JOB_GROUP = "julyScheduler";

    /**
     * Quartz scheduler (auto-configured by the quartz starter, RAMJobStore).
     */
    private final Scheduler scheduler;

    /**
     * Create the engine.
     *
     * @param scheduler quartz scheduler
     */
    public SchedulerEngine(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Register (or replace) a running task driven by the cron expression.
     *
     * @param id      task id
     * @param handler handler content (Spring bean name implementing Runnable)
     * @param cron    cron expression
     */
    @Override
    public void register(String id, String handler, String cron) {
        try {
            JobKey key = jobKey(id);

            if (scheduler.checkExists(key)) {
                scheduler.deleteJob(key);
            }

            JobDetail job = JobBuilder.newJob(SchedulerHandlerJob.class)
                    .withIdentity(key)
                    .usingJobData("id", id)
                    .usingJobData("handler", handler)
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + id, JOB_GROUP)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();

            scheduler.scheduleJob(job, Set.of(trigger), true);
        } catch (SchedulerException ex) {
            throw new IllegalStateException("scheduler register failed, id=" + id, ex);
        }
    }

    /**
     * Remove a task from the engine; missing jobs are ignored.
     *
     * @param id task id
     */
    @Override
    public void remove(String id) {
        try {
            scheduler.deleteJob(jobKey(id));
        } catch (SchedulerException ex) {
            throw new IllegalStateException("scheduler remove failed, id=" + id, ex);
        }
    }

    /**
     * Trigger the handler once immediately, regardless of the runtime status.
     *
     * @param id      task id
     * @param handler handler content (Spring bean name implementing Runnable)
     */
    @Override
    public void triggerOnce(String id, String handler) {
        try {
            JobKey key = jobKey(id);

            if (scheduler.checkExists(key)) {
                scheduler.triggerJob(key);
                return;
            }

            JobDetail job = JobBuilder.newJob(SchedulerHandlerJob.class)
                    .withIdentity(key)
                    .usingJobData("id", id)
                    .usingJobData("handler", handler)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("once_" + id, JOB_GROUP)
                    .startNow()
                    .build();

            scheduler.scheduleJob(job, Set.of(trigger), true);
        } catch (SchedulerException ex) {
            throw new IllegalStateException("scheduler trigger failed, id=" + id, ex);
        }
    }

    /**
     * Validate a cron expression.
     *
     * @param cron cron expression
     * @return true when the expression can be parsed
     */
    @Override
    public boolean validateCron(String cron) {
        return CronExpression.isValidExpression(cron);
    }

    /**
     * Build the job key for a task id.
     *
     * @param id task id
     * @return job key
     */
    private JobKey jobKey(String id) {
        return JobKey.jobKey("scheduler_" + id, JOB_GROUP);
    }
}

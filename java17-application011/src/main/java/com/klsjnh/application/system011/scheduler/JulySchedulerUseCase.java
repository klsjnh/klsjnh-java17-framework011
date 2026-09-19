package com.klsjnh.application.system011.scheduler;

/*                JulySchedulerUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.system011.scheduler.JulyScheduler;
import com.klsjnh.domain.system011.scheduler.JulySchedulerRepository;
import com.klsjnh.domain.system011.scheduler.SchedulerPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyScheduler use cases: CRUD plus start / stop / runOnce with scheduler
 * engine sync.
 * <p>
 * Every public method is a transaction boundary; the scheduler engine calls
 * are idempotent (in-memory engine, rebuildable from the database).
 * </p>
 */

@Service
public class JulySchedulerUseCase {

    /**
     * JulyScheduler repository.
     */
    private final JulySchedulerRepository repository;

    /**
     * Scheduler engine port.
     */
    private final SchedulerPort schedulerPort;

    /**
     * Create the use case.
     *
     * @param repository    july scheduler repository
     * @param schedulerPort scheduler engine port
     */
    public JulySchedulerUseCase(JulySchedulerRepository repository, SchedulerPort schedulerPort) {
        this.repository = repository;
        this.schedulerPort = schedulerPort;
    }

    /**
     * Insert a new scheduler task (defaults to stopped).
     *
     * @param schedulerCode    scheduler code, unique
     * @param schedulerName    scheduler name
     * @param schedulerHandler handler content (Spring bean name)
     * @param schedulerCron    cron expression
     * @return new task id
     */
    @Transactional
    public String insert(String schedulerCode, String schedulerName, String schedulerHandler, String schedulerCron) {
        validateCron(schedulerCron);

        if (repository.findByCode(schedulerCode) != null) {
            throw BusinessException.badRequest("scheduler code already exists: " + schedulerCode);
        }

        JulyScheduler scheduler = JulyScheduler.create(EntityId.generate(), schedulerCode, schedulerName,
                schedulerHandler, schedulerCron, AuditInfo.empty());
        repository.insert(scheduler);

        return scheduler.id().value();
    }

    /**
     * Update basics and runtime status; the engine is re-synced after save
     * (remove, then register again when the new status is running).
     *
     * @param id               task id
     * @param schedulerName    scheduler name
     * @param schedulerHandler handler content
     * @param schedulerCron    cron expression
     * @param status           runtime status ("1" running / "0" stopped)
     * @return updated task id
     */
    @Transactional
    public String update(String id, String schedulerName, String schedulerHandler, String schedulerCron, String status) {
        Status011 target = Status011.of(status);

        if (target == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }

        validateCron(schedulerCron);

        JulyScheduler scheduler = require(id);
        scheduler.updateBasics(schedulerName, schedulerHandler, schedulerCron);

        if (target == Status011.ENABLED) {
            scheduler.start();
        } else {
            scheduler.stop();
        }

        repository.update(scheduler);

        syncEngine(scheduler);

        return scheduler.id().value();
    }

    /**
     * Logic delete a single task; a running task is removed from the engine
     * first.
     *
     * @param id task id
     * @return deleted task id
     */
    @Transactional
    public String logicDelete(String id) {
        if (repository.findById(id) == null) {
            throw BusinessException.recordNotFound(id);
        }

        schedulerPort.remove(id);
        repository.logicDeleteById(id);

        return id;
    }

    /**
     * Logic delete tasks in batch, all-or-nothing: a missing id fails the whole
     * batch (404) so the transaction rolls back. On success each task is first
     * removed from the engine, then the rows are deleted in one statement.
     *
     * @param ids task ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        List<String> normalized = ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        for (String id : normalized) {
            if (repository.findById(id) == null) {
                throw BusinessException.recordNotFound(id);
            }
        }

        for (String id : normalized) {
            schedulerPort.remove(id);
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find by primary key.
     *
     * @param id task id
     * @return aggregate
     */
    public JulyScheduler getById(String id) {
        return require(id);
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param pageQuery    page query, null falls back to page 1 / size 10
     * @param codeKeyword  scheduler code keyword, nullable
     * @param nameKeyword  scheduler name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyScheduler> selectListByPage(PageQuery011 pageQuery, String codeKeyword,
            String nameKeyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyScheduler> rows = repository.findPage(query.offset(), query.pageSize(), codeKeyword, nameKeyword);
        long total = repository.count(codeKeyword, nameKeyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Start the task: status to running + register in the engine.
     *
     * @param id task id
     * @return started task id
     */
    @Transactional
    public String start(String id) {
        JulyScheduler scheduler = require(id);
        scheduler.start();
        repository.update(scheduler);
        schedulerPort.register(scheduler.id().value(), scheduler.schedulerHandler(), scheduler.schedulerCron());

        return scheduler.id().value();
    }

    /**
     * Stop the task: status to stopped + remove from the engine.
     *
     * @param id task id
     * @return stopped task id
     */
    @Transactional
    public String stop(String id) {
        JulyScheduler scheduler = require(id);
        scheduler.stop();
        repository.update(scheduler);
        schedulerPort.remove(scheduler.id().value());

        return scheduler.id().value();
    }

    /**
     * Trigger the handler once immediately; does not change the runtime status.
     *
     * @param id task id
     * @return triggered task id
     */
    public String runOnce(String id) {
        JulyScheduler scheduler = require(id);
        schedulerPort.triggerOnce(scheduler.id().value(), scheduler.schedulerHandler());

        return scheduler.id().value();
    }

    /**
     * Require an existing aggregate.
     *
     * @param id task id
     * @return aggregate
     */
    private JulyScheduler require(String id) {
        JulyScheduler scheduler = repository.findById(id);

        if (scheduler == null) {
            throw BusinessException.recordNotFound(id);
        }

        return scheduler;
    }

    /**
     * Validate the cron expression via the scheduler engine.
     *
     * @param cron cron expression
     */
    private void validateCron(String cron) {
        if (cron == null || cron.isBlank() || !schedulerPort.validateCron(cron)) {
            throw BusinessException.badRequest("invalid cron expression: " + cron);
        }
    }

    /**
     * Re-sync the engine after an update: remove the old registration, then
     * register again when the new status is running.
     *
     * @param scheduler updated aggregate
     */
    private void syncEngine(JulyScheduler scheduler) {
        schedulerPort.remove(scheduler.id().value());

        if (Status011.ENABLED.getCode().equals(scheduler.status())) {
            schedulerPort.register(scheduler.id().value(), scheduler.schedulerHandler(), scheduler.schedulerCron());
        }
    }
}

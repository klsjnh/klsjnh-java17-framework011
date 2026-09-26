package com.klsjnh.web.system011.controller;

/*                JulySchedulerController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler controller class
 *      2026.09.15  scheduler VO moved into the julyscheduler sub-package
 *      2026.09.24  pass operator id for permission checks
 *      2026.09.26  selectExecListByPage; runOnce @AuditLog (who) + child row (result)
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.system011.scheduler.JulySchedulerAuditUseCase;
import com.klsjnh.application.system011.scheduler.JulySchedulerUseCase;
import com.klsjnh.domain.system011.scheduler.JulyScheduler;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRow;

import com.klsjnh.web.system011.converter.JulySchedulerAuditConverter;
import com.klsjnh.web.system011.converter.JulySchedulerConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerAuditQueryVo011;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerAuditVo011;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerInsertVo011;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerQueryVo011;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerUpdateVo011;
import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * JulyScheduler HTTP adapter: scheduled task management endpoints (including
 * start / stop / runOnce) plus child-table execution-audit query. Ships in the
 * quartz scheduler starter only — without that dependency the routes are
 * absent from the classpath.
 * <p>
 * Audit split: start / stop / runOnce write {@code july_user_audit} via
 * {@code @AuditLog} (who). Actual job body outcomes write
 * {@code july_scheduler_audit} (result). Not platform audit.
 * </p>
 */

@Tag(name = "系统管理 - 定时任务")
@RestController
@RequestMapping("/klsjnh/system011/julyScheduler/v1")
public class JulySchedulerController {

    /**
     * JulyScheduler use case.
     */
    private final JulySchedulerUseCase julySchedulerUseCase;

    /**
     * Execution-audit use case (child table query).
     */
    private final JulySchedulerAuditUseCase julySchedulerAuditUseCase;

    /**
     * Response julySchedulerConverter.
     */
    private final JulySchedulerConverter julySchedulerConverter;

    /**
     * Response julySchedulerAuditConverter.
     */
    private final JulySchedulerAuditConverter julySchedulerAuditConverter;

    /**
     * Create the controller.
     *
     * @param julySchedulerUseCase         july scheduler use case
     * @param julySchedulerAuditUseCase    execution-audit use case
     * @param julySchedulerConverter       response julySchedulerConverter
     * @param julySchedulerAuditConverter  response julySchedulerAuditConverter
     */
    public JulySchedulerController(JulySchedulerUseCase julySchedulerUseCase,
            JulySchedulerAuditUseCase julySchedulerAuditUseCase, JulySchedulerConverter julySchedulerConverter,
            JulySchedulerAuditConverter julySchedulerAuditConverter) {
        this.julySchedulerUseCase = julySchedulerUseCase;
        this.julySchedulerAuditUseCase = julySchedulerAuditUseCase;
        this.julySchedulerConverter = julySchedulerConverter;
        this.julySchedulerAuditConverter = julySchedulerAuditConverter;
    }

    /**
     * Insert a new scheduled task (defaults to stopped).
     *
     * @param vo      insert request
     * @param request HTTP request (operator)
     * @return envelope with the new task id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/insert")
    @Operation(summary = "新增定时任务（默认停止态）")
    public Response011<IdVo011> insert(@Valid @RequestBody JulySchedulerInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName,
                julySchedulerUseCase.insert(operator.id(), vo.getSchedulerCode(), vo.getSchedulerName(),
                        vo.getSchedulerHandler(), vo.getSchedulerCron(), vo.getRemark()));
    }

    /**
     * Update a scheduled task (includes the runtime status; the engine is
     * re-synced when the status or cron/handler changes).
     *
     * @param vo      update request
     * @param request HTTP request (operator)
     * @return envelope with the updated task id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/update")
    @Operation(summary = "修改定时任务（含启停状态，变更触发调度引擎联动）")
    public Response011<IdVo011> update(@Valid @RequestBody JulySchedulerUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName,
                julySchedulerUseCase.update(operator.id(), vo.getId(), vo.getSchedulerName(), vo.getSchedulerHandler(),
                        vo.getSchedulerCron(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a single task; a running task is removed from the engine
     * first.
     *
     * @param idVo    request with the task id
     * @param request HTTP request (operator)
     * @return envelope with the deleted task id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（单个，运行中任务先摘出调度引擎）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julySchedulerUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Logic delete tasks in batch; per-id failure is reported, not thrown.
     *
     * @param idsVo   request with the task id list
     * @param request HTTP request (operator)
     * @return per-id success/failure summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除（批量，运行中任务先摘出调度引擎）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@Valid @RequestBody IdsVo011 idsVo,
            HttpServletRequest request) {
        String funcName = "logic delete batch";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julySchedulerUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }

    /**
     * Find a task by primary key (safe + idempotent, hence GET).
     *
     * @param id      task id, passed as a query parameter
     * @param request HTTP request (operator)
     * @return task detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulySchedulerVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName,
                julySchedulerConverter.toVo(julySchedulerUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param query   page query request
     * @param request HTTP request (operator)
     * @return page result of tasks
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（任务编码/名称模糊过滤）")
    public Response011<PageResult011<JulySchedulerVo011>> selectListByPage(
            @Valid @RequestBody JulySchedulerQueryVo011 query, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyScheduler> page = julySchedulerUseCase.selectListByPage(operator.id(), pageQuery,
                query.getSchedulerCode(), query.getSchedulerName());

        return Response011.success(funcName, page.withRows(julySchedulerConverter.toVoList(page.rows())));
    }

    /**
     * Start a task: register it in the scheduler engine.
     *
     * @param idVo    request with the task id
     * @param request HTTP request (operator)
     * @return envelope with the started task id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/start")
    @Operation(summary = "启动任务（注册调度引擎）")
    public Response011<IdVo011> start(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "start";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julySchedulerUseCase.start(operator.id(), idVo.getId()));
    }

    /**
     * Stop a task: remove it from the scheduler engine.
     *
     * @param idVo    request with the task id
     * @param request HTTP request (operator)
     * @return envelope with the stopped task id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/stop")
    @Operation(summary = "停止任务（摘出调度引擎）")
    public Response011<IdVo011> stop(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "stop";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julySchedulerUseCase.stop(operator.id(), idVo.getId()));
    }

    /**
     * Trigger the handler once immediately. {@code @AuditLog} records who
     * requested; the child exec-audit row is written when the job finishes.
     *
     * @param idVo    request with the task id
     * @param request HTTP request (operator)
     * @return envelope with the triggered task id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_SCHEDULER)
    @PostMapping("/runOnce")
    @Operation(summary = "立即执行一次（不改变运行态；用户审计记谁 + 子表记结果）")
    public Response011<IdVo011> runOnce(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "run once";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julySchedulerUseCase.runOnce(operator.id(), idVo.getId()));
    }

    /**
     * Page query of execution-audit rows for one scheduler definition
     * (master-sub select).
     *
     * @param query   page query with required pkMt
     * @param request HTTP request (operator)
     * @return page result of run outcomes
     */
    @PostMapping("/selectExecListByPage")
    @Operation(summary = "执行审计分页（按主表 pk_mt；SUCCESS/FAIL 子表）")
    public Response011<PageResult011<JulySchedulerAuditVo011>> selectExecListByPage(
            @Valid @RequestBody JulySchedulerAuditQueryVo011 query, HttpServletRequest request) {
        String funcName = "select exec list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulySchedulerAuditRow> page = julySchedulerAuditUseCase.selectListByPage(operator.id(),
                pageQuery, query.getPkMt());

        return Response011.success(funcName, page.withRows(julySchedulerAuditConverter.toVoList(page.rows())));
    }
}

package com.klsjnh.web.system011.controller;

/*                JulySchedulerController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.scheduler.JulySchedulerUseCase;
import com.klsjnh.domain.scheduler.JulyScheduler;
import com.klsjnh.web.system011.converter.JulySchedulerConverter;
import com.klsjnh.web.system011.vo.JulySchedulerInsertVo011;
import com.klsjnh.web.system011.vo.JulySchedulerQueryVo011;
import com.klsjnh.web.system011.vo.JulySchedulerUpdateVo011;
import com.klsjnh.web.system011.vo.JulySchedulerVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * JulyScheduler HTTP adapter: scheduled task management endpoints. The
 * controller only receives and forwards requests — mapping lives in the
 * converter, orchestration in the use case.
 */

@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/klsjnh/system011/julyScheduler/v1")
public class JulySchedulerController {

    /**
     * JulyScheduler use case.
     */
    private final JulySchedulerUseCase useCase;

    /**
     * Response converter.
     */
    private final JulySchedulerConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july scheduler use case
     * @param converter response converter
     */
    public JulySchedulerController(JulySchedulerUseCase useCase, JulySchedulerConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new scheduled task (defaults to stopped).
     *
     * @param vo insert request
     * @return envelope with the new task id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增定时任务（默认停止态）")
    public Response011<IdVo011> insert(@RequestBody JulySchedulerInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getSchedulerCode(), vo.getSchedulerName(),
                vo.getSchedulerHandler(), vo.getSchedulerCron()));
    }

    /**
     * Update a scheduled task (includes the runtime status; the engine is
     * re-synced when the status or cron/handler changes).
     *
     * @param vo update request
     * @return empty envelope
     */
    @PostMapping("/update")
    @Operation(summary = "修改定时任务（含启停状态，变更触发调度引擎联动）")
    public Response011<Void> update(@RequestBody JulySchedulerUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getSchedulerName(), vo.getSchedulerHandler(), vo.getSchedulerCron(),
                vo.getStatus());

        return Response011.success(funcName, null);
    }

    /**
     * Logic delete tasks (batch).
     *
     * @param ids task id list
     * @return per-id success/failure summary
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（批量，运行中任务先摘出调度引擎）")
    public Response011<BatchDeleteResultVo011> logicDelete(@RequestBody List<String> ids) {
        String funcName = "logic delete";

        return Response011.success(funcName, useCase.logicDelete(ids));
    }

    /**
     * Find a task by primary key.
     *
     * @param idVo request with the task id
     * @return task detail
     */
    @PostMapping("/getById")
    @Operation(summary = "主键查询")
    public Response011<JulySchedulerVo011> getById(@RequestBody IdVo011 idVo) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(idVo.getId())));
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param query page query request
     * @return page result of tasks
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（任务编码/名称模糊过滤）")
    public Response011<PageResult011<JulySchedulerVo011>> selectListByPage(
            @RequestBody JulySchedulerQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyScheduler> page = useCase.selectListByPage(pageQuery, query.getSchedulerCode(),
                query.getSchedulerName());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Start a task: register it in the scheduler engine.
     *
     * @param idVo request with the task id
     * @return empty envelope
     */
    @PostMapping("/start")
    @Operation(summary = "启动任务（注册调度引擎）")
    public Response011<Void> start(@RequestBody IdVo011 idVo) {
        useCase.start(idVo.getId());

        return Response011.success("start", null);
    }

    /**
     * Stop a task: remove it from the scheduler engine.
     *
     * @param idVo request with the task id
     * @return empty envelope
     */
    @PostMapping("/stop")
    @Operation(summary = "停止任务（摘出调度引擎）")
    public Response011<Void> stop(@RequestBody IdVo011 idVo) {
        useCase.stop(idVo.getId());

        return Response011.success("stop", null);
    }

    /**
     * Trigger the handler once immediately.
     *
     * @param idVo request with the task id
     * @return empty envelope
     */
    @PostMapping("/runOnce")
    @Operation(summary = "立即执行一次（不改变运行态）")
    public Response011<Void> runOnce(@RequestBody IdVo011 idVo) {
        useCase.runOnce(idVo.getId());

        return Response011.success("run once", null);
    }
}

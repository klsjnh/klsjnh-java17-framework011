package com.klsjnh.web.datasource.controller;

/*                JulySyncController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule controller
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.datasource.sync.JulySyncRuleUseCase;
import com.klsjnh.application.datasource.sync.SyncColumnCommand;
import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleQuerySpec;
import com.klsjnh.domain.datasource.sync.SyncRunResult;

import com.klsjnh.web.datasource.vo.julysync.JulySyncColumnVo011;
import com.klsjnh.web.datasource.vo.julysync.JulySyncInsertVo011;
import com.klsjnh.web.datasource.vo.julysync.JulySyncQueryVo011;
import com.klsjnh.web.datasource.vo.julysync.JulySyncRuleVo011;
import com.klsjnh.web.datasource.vo.julysync.JulySyncRunVo011;
import com.klsjnh.web.datasource.vo.julysync.JulySyncSaveWholeVo011;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Sync rule HTTP adapter ({@code /klsjnh/datasource/julySync/v1}): rule CRUD
 * (with the column mapping) and running a rule once (single-table sync S1).
 * Permission checks live in the use case (operator resolved here).
 */

@Tag(name = "数据源011 - 同步")
@RestController
@RequestMapping("/klsjnh/datasource/julySync/v1")
public class JulySyncController {

    /**
     * Sync rule use case.
     */
    private final JulySyncRuleUseCase syncRuleUseCase;

    /**
     * Create the controller.
     *
     * @param syncRuleUseCase sync rule use case
     */
    public JulySyncController(JulySyncRuleUseCase syncRuleUseCase) {
        this.syncRuleUseCase = syncRuleUseCase;
    }

    /**
     * Insert a sync rule.
     *
     * @param vo      request
     * @param request http request
     * @return new rule id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增同步规则（含列对照）")
    public Response011<IdVo011> insert(@RequestBody JulySyncInsertVo011 vo, HttpServletRequest request) {
        String funcName = "sync rule insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        List<SyncColumnCommand> columns = new ArrayList<>();

        if (vo.getColumns() != null) {
            for (JulySyncColumnVo011 column : vo.getColumns()) {
                columns.add(new SyncColumnCommand(column.getSourceColumn(), column.getSourceType(),
                        column.getTargetColumn(), column.getTargetType(), column.getTransform(), column.getSortOrder()));
            }
        }

        String id = syncRuleUseCase.insert(operator.id(), vo.getSyncCode(), vo.getSyncName(), vo.getSourceDsCode(),
                vo.getSourceKind(), vo.getSourceData(), vo.getTargetDsCode(), vo.getTargetKind(), vo.getTargetData(),
                vo.getMode(), vo.getSyncKey(), vo.getConflict(), vo.getPageSize(), vo.getRemark(), columns);

        return Response011.successId(funcName, id);
    }

    /**
     * Page query sync rules.
     *
     * @param vo      request
     * @param request http request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "同步规则分页（keyword/status）")
    public Response011<PageResult011<JulySyncRuleVo011>> selectListByPage(@RequestBody JulySyncQueryVo011 vo,
            HttpServletRequest request) {
        String funcName = "sync rule select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulySyncRuleQuerySpec spec = new JulySyncRuleQuerySpec(vo.getKeyword(), vo.getStatus());
        PageResult011<JulySyncRule> page = syncRuleUseCase.selectListByPage(operator.id(), query, spec);

        List<JulySyncRuleVo011> rows = page.rows().stream().map(this::toVo).toList();

        return Response011.success(funcName,
                PageResult011.of(query, page.total(), rows));
    }

    /**
     * Find a rule by sync code.
     *
     * @param syncCode sync code
     * @param request  http request
     * @return rule
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按同步编码点查")
    public Response011<JulySyncRuleVo011> getByCode(@RequestParam("syncCode") String syncCode,
            HttpServletRequest request) {
        String funcName = "sync rule get by code";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, toVo(syncRuleUseCase.getByCode(operator.id(), syncCode)));
    }

    /**
     * Whole save: rule + column mappings in one transaction (columns replaced).
     *
     * @param vo      request
     * @param request http request
     * @return envelope with the rule id
     */
    @PostMapping("/saveWhole")
    @Operation(summary = "整存同步规则 + 列映射（主+子，一个事务；子表替换）")
    public Response011<IdVo011> saveWhole(@RequestBody JulySyncSaveWholeVo011 vo, HttpServletRequest request) {
        String funcName = "sync rule save whole";
        Operator011 operator = Operator011Resolver.resolve(request);

        List<SyncColumnCommand> columns = new ArrayList<>();

        if (vo.getColumns() != null) {
            for (JulySyncColumnVo011 column : vo.getColumns()) {
                columns.add(new SyncColumnCommand(column.getSourceColumn(), column.getSourceType(),
                        column.getTargetColumn(), column.getTargetType(), column.getTransform(), column.getSortOrder()));
            }
        }

        String id = syncRuleUseCase.saveWhole(operator.id(), vo.getId(), vo.getSyncCode(), vo.getSyncName(),
                vo.getSourceDsCode(), vo.getSourceKind(), vo.getSourceData(), vo.getTargetDsCode(), vo.getTargetKind(),
                vo.getTargetData(), vo.getMode(), vo.getSyncKey(), vo.getConflict(), vo.getPageSize(), vo.getRemark(),
                vo.getStatus(), columns);

        return Response011.successId(funcName, id);
    }

    /**
     * Read a rule together with its column mappings (master + children).
     *
     * @param id      rule id
     * @param request http request
     * @return envelope with the rule and its column list
     */
    @GetMapping("/getWithChildren")
    @Operation(summary = "主+子联查（同步规则 + 列映射）")
    public Response011<java.util.Map<String, Object>> getWithChildren(@RequestParam("id") String id,
            HttpServletRequest request) {
        String funcName = "sync rule get with children";
        Operator011 operator = Operator011Resolver.resolve(request);

        java.util.Map<String, Object> source = syncRuleUseCase.getWithChildren(operator.id(), id);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("master", toVo((JulySyncRule) source.get("master")));

        List<JulySyncColumnVo011> columns = new ArrayList<>();

        for (Object row : (List<?>) source.get("columns")) {
            columns.add(toColumnVo((com.klsjnh.domain.datasource.sync.JulySyncRuleColumn) row));
        }

        result.put("columns", columns);

        return Response011.success(funcName, result);
    }

    /**
     * Map a column aggregate to its response VO.
     *
     * @param column column aggregate
     * @return response VO
     */
    private JulySyncColumnVo011 toColumnVo(com.klsjnh.domain.datasource.sync.JulySyncRuleColumn column) {
        JulySyncColumnVo011 vo = new JulySyncColumnVo011();
        vo.setSourceColumn(column.sourceColumn());
        vo.setSourceType(column.sourceType());
        vo.setTargetColumn(column.targetColumn());
        vo.setTargetType(column.targetType());
        vo.setTransform(column.transform());
        vo.setSortOrder(column.sortOrder());

        return vo;
    }

    /**
     * Logic delete a rule.
     *
     * @param idVo    id
     * @param request http request
     * @return deleted id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除同步规则（含列对照）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "sync rule logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, syncRuleUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Run a rule once.
     *
     * @param vo      request
     * @param request http request
     * @return run result
     */
    @PostMapping("/run")
    @Operation(summary = "执行同步规则一次（单表：源分页读 → 列对照 → 目标 upsert）")
    public Response011<SyncRunResult> run(@RequestBody JulySyncRunVo011 vo, HttpServletRequest request) {
        String funcName = "sync rule run";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, syncRuleUseCase.run(operator.id(), vo.getSyncCode()));
    }

    /**
     * Map the aggregate to the response VO.
     *
     * @param rule aggregate
     * @return response VO
     */
    private JulySyncRuleVo011 toVo(JulySyncRule rule) {
        JulySyncRuleVo011 vo = new JulySyncRuleVo011();
        vo.setId(rule.id().value());
        vo.setSyncCode(rule.syncCode());
        vo.setSyncName(rule.syncName());
        vo.setSourceDsCode(rule.sourceDsCode());
        vo.setSourceKind(rule.sourceKind());
        vo.setSourceData(rule.sourceData());
        vo.setTargetDsCode(rule.targetDsCode());
        vo.setTargetKind(rule.targetKind());
        vo.setTargetData(rule.targetData());
        vo.setMode(rule.mode());
        vo.setSyncKey(rule.syncKey());
        vo.setConflict(rule.conflict());
        vo.setPageSize(rule.pageSize());
        vo.setStatus(rule.status());

        return vo;
    }
}

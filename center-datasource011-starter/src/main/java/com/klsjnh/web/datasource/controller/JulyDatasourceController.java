package com.klsjnh.web.datasource.controller;

/*                JulyDatasourceController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource controller class
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.datasource.management.JulyDatasourceUseCase;
import com.klsjnh.domain.datasource.management.JulyDatasource;
import com.klsjnh.domain.datasource.management.JulyDatasourceQuerySpec;
import com.klsjnh.domain.datasource.kernel.DataSourceProbePort;
import com.klsjnh.domain.datasource.kernel.ReloadResult;

import com.klsjnh.web.datasource.converter.JulyDatasourceConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceVo011;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceTestVo011;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceQueryVo011;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceInsertVo011;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceUpdateVo011;
import com.klsjnh.web.datasource.vo.julydatasource.JulyDatasourceTestResultVo011;
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
 * JulyDatasource HTTP adapter: admin CRUD over the runtime datasource
 * registry, plus connectivity test and an explicit registry reload.
 * Permission checks live in the use case (operator resolved here).
 */

@Tag(name = "数据源011 - 数据源管理")
@RestController
@RequestMapping("/klsjnh/datasource/julyDatasource/v1")
public class JulyDatasourceController {

    /**
     * JulyDatasource use case.
     */
    private final JulyDatasourceUseCase julyDatasourceUseCase;

    /**
     * Response julyDatasourceConverter.
     */
    private final JulyDatasourceConverter julyDatasourceConverter;

    /**
     * Create the controller.
     *
     * @param julyDatasourceUseCase   july datasource use case
     * @param julyDatasourceConverter response julyDatasourceConverter
     */
    public JulyDatasourceController(JulyDatasourceUseCase julyDatasourceUseCase, JulyDatasourceConverter julyDatasourceConverter) {
        this.julyDatasourceUseCase = julyDatasourceUseCase;
        this.julyDatasourceConverter = julyDatasourceConverter;
    }

    /**
     * Insert a new datasource entry (dsCode uniqueness checked, registry
     * refreshed).
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new datasource id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/insert")
    @Operation(summary = "新增数据源（dsCode 查重；落库后刷新注册表）")
    public Response011<IdVo011> insert(@Valid @RequestBody JulyDatasourceInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        String id = julyDatasourceUseCase.insert(operator.id(), vo.getDsCode(), vo.getSortOrder(), vo.getDsName(),
                vo.getDbType(), vo.getJdbcUrl(), vo.getSchemaName(), vo.getUsername(), vo.getPassword(),
                vo.getDriverClass(), vo.getRemark());

        return Response011.successId(funcName, id);
    }

    /**
     * Update a datasource entry (dsCode immutable; blank password keeps the
     * stored one; registry refreshed).
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the datasource id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/update")
    @Operation(summary = "修改数据源（dsCode 不可变；密码留空保持原值；落库后刷新注册表）")
    public Response011<IdVo011> update(@Valid @RequestBody JulyDatasourceUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        String id = julyDatasourceUseCase.update(operator.id(), vo.getId(), vo.getDsName(), vo.getSortOrder(),
                vo.getDbType(), vo.getJdbcUrl(), vo.getSchemaName(), vo.getUsername(), vo.getPassword(),
                vo.getDriverClass(), vo.getRemark());

        return Response011.successId(funcName, id);
    }

    /**
     * Logic delete a datasource entry (registry refreshed).
     *
     * @param idVo    request with the datasource id
     * @param request http request
     * @return envelope with the datasource id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（落库后刷新注册表，运行时应立即摘除该数据源）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyDatasourceUseCase.logicDelete(operator.id(), idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Batch logic delete with a per-id summary and ONE registry refresh.
     *
     * @param idsVo   request with the datasource ids
     * @param request http request
     * @return per-id summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "批量逻辑删除（逐条判定，整批只刷新一次注册表）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@Valid @RequestBody IdsVo011 idsVo, HttpServletRequest request) {
        String funcName = "logic delete batch";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyDatasourceUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }

    /**
     * Find a datasource by primary key (safe + idempotent, hence GET).
     *
     * @param id      datasource id, passed as a query parameter
     * @param request http request
     * @return datasource detail (no password field)
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query；出参不含密码）")
    public Response011<JulyDatasourceVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyDatasourceConverter.toVo(julyDatasourceUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query on the management view (disabled rows included).
     *
     * @param vo      page query request
     * @param request http request
     * @return page result of datasource rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码 / 名称 / URL 模糊 + 状态过滤）")
    public Response011<PageResult011<JulyDatasourceVo011>> selectListByPage(@Valid @RequestBody JulyDatasourceQueryVo011 vo,
            HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyDatasourceQuerySpec spec = new JulyDatasourceQuerySpec(vo.getKeyword(), vo.getStatus());
        PageResult011<JulyDatasource> page = julyDatasourceUseCase.selectListByPage(operator.id(), pageQuery, spec);

        return Response011.success(funcName, page.withRows(julyDatasourceConverter.toVoList(page.rows())));
    }

    /**
     * Test connectivity: draft path when no id is given (nothing is saved),
     * saved-retest path when an id is given (blank password falls back to the
     * stored one).
     *
     * @param vo      connectivity test request
     * @param request http request
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接（不传 id 为草稿态测试；传 id 为重测已保存数据源；不回灌注册表）")
    public Response011<JulyDatasourceTestResultVo011> testConnection(@Valid @RequestBody JulyDatasourceTestVo011 vo,
            HttpServletRequest request) {
        String funcName = "test connection";
        Operator011 operator = Operator011Resolver.resolve(request);

        DataSourceProbePort.ProbeResult result = StringUtil011.isBlank(vo.getId())
                ? julyDatasourceUseCase.testDraft(operator.id(), vo.getDsCode(), vo.getDbType(), vo.getJdbcUrl(),
                        vo.getUsername(), vo.getPassword(), vo.getDriverClass())
                : julyDatasourceUseCase.testSaved(operator.id(), vo.getId(), vo.getPassword());

        return Response011.success(funcName, julyDatasourceConverter.toTestResultVo(result));
    }

    /**
     * Reload the runtime registry from the table (the table-driven entry —
     * useful after a direct SQL edit of july_datasource).
     *
     * @param request http request
     * @return reconciliation summary
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/reloadRegistry")
    @Operation(summary = "重载注册表（按 july_datasource 启用行重建声明集，复用未变动的连接池）")
    public Response011<ReloadResult> reloadRegistry(HttpServletRequest request) {
        String funcName = "reload registry";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyDatasourceUseCase.reloadRegistry(operator.id()));
    }
}

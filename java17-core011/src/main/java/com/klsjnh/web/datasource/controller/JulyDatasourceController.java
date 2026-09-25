package com.klsjnh.web.datasource.controller;

/*                JulyDatasourceController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource controller class
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyDatasource HTTP adapter: admin CRUD over the runtime datasource
 * registry, plus connectivity test and an explicit registry reload.
 * <p>
 * There is deliberately NO getByCode HTTP endpoint — reading a datasource by
 * code is a program behavior (the routing layer / registry reload), not a
 * management action.
 * </p>
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
     * @param vo insert request
     * @return envelope with the new datasource id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/insert")
    @Operation(summary = "新增数据源（dsCode 查重；落库后刷新注册表）")
    public Response011<IdVo011> insert(@RequestBody JulyDatasourceInsertVo011 vo) {
        String funcName = "insert";

        String id = julyDatasourceUseCase.insert(vo.getDsCode(), vo.getSortOrder(), vo.getDsName(), vo.getDbType(), vo.getJdbcUrl(),
                vo.getSchemaName(), vo.getUsername(), vo.getPassword(), vo.getDriverClass(), vo.getRemark());

        return Response011.successId(funcName, id);
    }

    /**
     * Update a datasource entry (dsCode immutable; blank password keeps the
     * stored one; registry refreshed).
     *
     * @param vo update request
     * @return envelope with the datasource id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/update")
    @Operation(summary = "修改数据源（dsCode 不可变；密码留空保持原值；落库后刷新注册表）")
    public Response011<IdVo011> update(@RequestBody JulyDatasourceUpdateVo011 vo) {
        String funcName = "update";

        String id = julyDatasourceUseCase.update(vo.getId(), vo.getDsName(), vo.getSortOrder(), vo.getDbType(), vo.getJdbcUrl(),
                vo.getSchemaName(), vo.getUsername(), vo.getPassword(), vo.getDriverClass(), vo.getRemark());

        return Response011.successId(funcName, id);
    }

    /**
     * Logic delete a datasource entry (registry refreshed).
     *
     * @param idVo request with the datasource id
     * @return envelope with the datasource id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（落库后刷新注册表，运行时应立即摘除该数据源）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        julyDatasourceUseCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Batch logic delete with a per-id summary and ONE registry refresh.
     *
     * @param idsVo request with the datasource ids
     * @return per-id summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "批量逻辑删除（逐条判定，整批只刷新一次注册表）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, julyDatasourceUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a datasource by primary key (safe + idempotent, hence GET).
     *
     * @param id datasource id, passed as a query parameter
     * @return datasource detail (no password field)
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query；出参不含密码）")
    public Response011<JulyDatasourceVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyDatasourceConverter.toVo(julyDatasourceUseCase.getById(id)));
    }

    /**
     * Page query on the management view (disabled rows included).
     *
     * @param vo page query request
     * @return page result of datasource rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码 / 名称 / URL 模糊 + 状态过滤）")
    public Response011<PageResult011<JulyDatasourceVo011>> selectListByPage(@RequestBody JulyDatasourceQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyDatasourceQuerySpec spec = new JulyDatasourceQuerySpec(vo.getKeyword(), vo.getStatus());
        PageResult011<JulyDatasource> page = julyDatasourceUseCase.selectListByPage(pageQuery, spec);

        return Response011.success(funcName, page.withRows(julyDatasourceConverter.toVoList(page.rows())));
    }

    /**
     * Test connectivity: draft path when no id is given (nothing is saved),
     * saved-retest path when an id is given (blank password falls back to the
     * stored one).
     * <p>
     * An unreachable target returns 200 with {@code data.success=false} — the
     * probe action itself succeeded.
     * </p>
     *
     * @param vo connectivity test request
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接（不传 id 为草稿态测试；传 id 为重测已保存数据源；不回灌注册表）")
    public Response011<JulyDatasourceTestResultVo011> testConnection(@RequestBody JulyDatasourceTestVo011 vo) {
        String funcName = "test connection";

        DataSourceProbePort.ProbeResult result = StringUtil011.isBlank(vo.getId())
                ? julyDatasourceUseCase.testDraft(vo.getDsCode(), vo.getDbType(), vo.getJdbcUrl(), vo.getUsername(),
                        vo.getPassword(), vo.getDriverClass())
                : julyDatasourceUseCase.testSaved(vo.getId(), vo.getPassword());

        return Response011.success(funcName, julyDatasourceConverter.toTestResultVo(result));
    }

    /**
     * Reload the runtime registry from the table (the table-driven entry —
     * useful after a direct SQL edit of july_datasource).
     *
     * @return reconciliation summary
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DATASOURCE)
    @PostMapping("/reloadRegistry")
    @Operation(summary = "重载注册表（按 july_datasource 启用行重建声明集，复用未变动的连接池）")
    public Response011<ReloadResult> reloadRegistry() {
        String funcName = "reload registry";

        return Response011.success(funcName, julyDatasourceUseCase.reloadRegistry());
    }
}

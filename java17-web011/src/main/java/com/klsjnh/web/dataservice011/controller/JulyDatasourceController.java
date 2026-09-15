package com.klsjnh.web.dataservice011.controller;

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

import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.dataservice011.JulyDatasourceUseCase;
import com.klsjnh.domain.dataservice011.JulyDatasource;
import com.klsjnh.domain.dataservice011.JulyDatasourceQuerySpec;
import com.klsjnh.domain.datasource.DataSourceProbePort;
import com.klsjnh.domain.datasource.ReloadResult;

import com.klsjnh.web.dataservice011.converter.JulyDatasourceConverter;

import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceTestVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceQueryVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceInsertVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceUpdateVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceTestResultVo011;

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

@Tag(name = "数据服务011 - 数据源管理")
@RestController
@RequestMapping("/klsjnh/dataservice011/julyDatasource/v1")
public class JulyDatasourceController {

    /**
     * JulyDatasource use case.
     */
    private final JulyDatasourceUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyDatasourceConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july datasource use case
     * @param converter response converter
     */
    public JulyDatasourceController(JulyDatasourceUseCase useCase, JulyDatasourceConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new datasource entry (dsCode uniqueness checked, registry
     * refreshed).
     *
     * @param vo insert request
     * @return envelope with the new datasource id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增数据源（dsCode 查重；落库后刷新注册表）")
    public Response011<IdVo011> insert(@RequestBody JulyDatasourceInsertVo011 vo) {
        String funcName = "insert";

        String id = useCase.insert(vo.getDsCode(), vo.getDsName(), vo.getDbType(), vo.getJdbcUrl(),
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
    @PostMapping("/update")
    @Operation(summary = "修改数据源（dsCode 不可变；密码留空保持原值；落库后刷新注册表）")
    public Response011<IdVo011> update(@RequestBody JulyDatasourceUpdateVo011 vo) {
        String funcName = "update";

        String id = useCase.update(vo.getId(), vo.getDsName(), vo.getDbType(), vo.getJdbcUrl(), vo.getSchemaName(),
                vo.getUsername(), vo.getPassword(), vo.getDriverClass(), vo.getRemark());

        return Response011.successId(funcName, id);
    }

    /**
     * Logic delete a datasource entry (registry refreshed).
     *
     * @param idVo request with the datasource id
     * @return envelope with the datasource id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（落库后刷新注册表，运行时应立即摘除该数据源）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        useCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Batch logic delete with a per-id summary and ONE registry refresh.
     *
     * @param idsVo request with the datasource ids
     * @return per-id summary
     */
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "批量逻辑删除（逐条判定，整批只刷新一次注册表）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, useCase.logicDeleteBatch(idsVo.getIds()));
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

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
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
        PageResult011<JulyDatasource> page = useCase.selectListByPage(pageQuery, spec);

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
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
                ? useCase.testDraft(vo.getDsCode(), vo.getDbType(), vo.getJdbcUrl(), vo.getUsername(),
                        vo.getPassword(), vo.getDriverClass())
                : useCase.testSaved(vo.getId(), vo.getPassword());

        return Response011.success(funcName, converter.toTestResultVo(result));
    }

    /**
     * Reload the runtime registry from the table (the table-driven entry —
     * useful after a direct SQL edit of july_datasource).
     *
     * @return reconciliation summary
     */
    @PostMapping("/reloadRegistry")
    @Operation(summary = "重载注册表（按 july_datasource 启用行重建声明集，复用未变动的连接池）")
    public Response011<ReloadResult> reloadRegistry() {
        String funcName = "reload registry";

        return Response011.success(funcName, useCase.reloadRegistry());
    }
}

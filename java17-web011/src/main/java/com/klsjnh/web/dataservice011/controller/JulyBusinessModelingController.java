package com.klsjnh.web.dataservice011.controller;

/*                JulyBusinessModelingController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.dataservice011.JulyBusinessModelingUseCase;
import com.klsjnh.domain.dataservice011.JulyBusinessModeling;
import com.klsjnh.domain.dataservice011.JulyBusinessModelingQuerySpec;

import com.klsjnh.web.dataservice011.converter.JulyBusinessModelingConverter;

import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingExecuteVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingInsertVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingPageExecuteVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingProbeResultVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingProbeVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingQueryVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingResultVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingUpdateVo011;
import com.klsjnh.web.dataservice011.vo.julybusinessmodeling.JulyBusinessModelingVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * JulyBusinessModeling HTTP adapter: modeling CRUD, the SQL probe inference, the
 * direct and paged SQL execution, and the hand-over product read.
 */

@Tag(name = "数据服务011 - 业务建模")
@RestController
@RequestMapping("/klsjnh/dataservice011/julyBusinessModeling/v1")
public class JulyBusinessModelingController {

    /**
     * JulyBusinessModeling use case.
     */
    private final JulyBusinessModelingUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyBusinessModelingConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july business modeling use case
     * @param converter response converter
     */
    public JulyBusinessModelingController(JulyBusinessModelingUseCase useCase,
            JulyBusinessModelingConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new modeling entry.
     *
     * @param vo insert request
     * @return envelope with the new modeling id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增建模（modelCode / objectName 查重，数据源须启用）")
    public Response011<IdVo011> insert(@RequestBody JulyBusinessModelingInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getModelCode(), vo.getModelName(),
                vo.getDataSourceCode(), vo.getSqlContent(), vo.getObjectName(), vo.getObjectType(),
                vo.getObjectDescription(), vo.getPackageName(), vo.getBusinessField(), vo.getRouterPath(),
                vo.getRemark(), converter.toFields(vo.getFieldData())));
    }

    /**
     * Update a modeling entry.
     *
     * @param vo update request
     * @return envelope with the modeling id
     */
    @PostMapping("/update")
    @Operation(summary = "修改建模（modelCode / objectName 不可变；明细整体替换）")
    public Response011<IdVo011> update(@RequestBody JulyBusinessModelingUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, useCase.update(vo.getId(), vo.getModelName(), vo.getDataSourceCode(),
                vo.getSqlContent(), vo.getObjectType(), vo.getObjectDescription(), vo.getPackageName(),
                vo.getBusinessField(), vo.getRouterPath(), vo.getRemark(), converter.toFields(vo.getFieldData())));
    }

    /**
     * Logic delete a modeling entry.
     *
     * @param idVo request with the modeling id
     * @return envelope with the deleted modeling id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（单条）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, useCase.logicDelete(idVo.getId()));
    }

    /**
     * Logic delete modeling entries in batch.
     *
     * @param idsVo request with the modeling ids
     * @return per-id summary
     */
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除（批量，逐条回报）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, useCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a modeling entry by primary key (GET).
     *
     * @param id modeling id, passed as a query parameter
     * @return modeling detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query，含 sqlContent 与产物）")
    public Response011<JulyBusinessModelingVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
    }

    /**
     * Find a modeling entry by modeling code (GET).
     *
     * @param code modeling code, passed as a query parameter
     * @return modeling detail
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按 modelCode 点查（code 走 query）")
    public Response011<JulyBusinessModelingVo011> getByCode(@RequestParam("code") String code) {
        String funcName = "get by code";

        return Response011.success(funcName, converter.toVo(useCase.getByCode(code)));
    }

    /**
     * Page query of modeling records.
     *
     * @param vo page query request
     * @return page result of modeling records
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询建模列表（关键字 + 数据源 + 状态过滤）")
    public Response011<PageResult011<JulyBusinessModelingVo011>> selectListByPage(
            @RequestBody JulyBusinessModelingQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyBusinessModeling> page = useCase.selectListByPage(pageQuery,
                new JulyBusinessModelingQuerySpec(vo.getKeyword(), vo.getDataSourceCode(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Probe a draft SQL and infer the fields (nothing persisted).
     *
     * @param vo probe request
     * @return probe result
     */
    @PostMapping("/probe")
    @Operation(summary = "SQL 探针推断（草稿态可用，不落库）")
    public Response011<JulyBusinessModelingProbeResultVo011> probe(@RequestBody JulyBusinessModelingProbeVo011 vo) {
        String funcName = "probe";

        return Response011.success(funcName,
                converter.toProbeResultVo(useCase.probeAndInfer(vo.getDataSourceCode(), vo.getSqlContent(),
                        vo.getObjectName())));
    }

    /**
     * Hand-over product read: metaData + fieldData.
     *
     * @param id modeling id, passed as a query parameter
     * @return product VO
     */
    @GetMapping("/getModelData")
    @Operation(summary = "交接产物（metaData + fieldData 两段，id 走 query）")
    public Response011<JulyBusinessModelingVo011> getModelData(@RequestParam("id") String id) {
        String funcName = "get model data";

        return Response011.success(funcName, converter.toVo(useCase.getModelData(id)));
    }

    /**
     * Execute a read-only SQL against the bound business datasource.
     *
     * @param vo execute request
     * @return result set
     */
    @PostMapping("/executeSql")
    @Operation(summary = "执行 SQL（业务库，返回结果集；超 500 行引导走分页）")
    public Response011<JulyBusinessModelingResultVo011> executeSql(@RequestBody JulyBusinessModelingExecuteVo011 vo) {
        String funcName = "execute sql";

        List<Map<String, Object>> rows = useCase.executeSql(vo.getDataSourceCode(), vo.getSqlContent());

        return Response011.success(funcName, converter.toResultVo(rows));
    }

    /**
     * Execute a read-only SQL with dialect paging against the bound business
     * datasource.
     *
     * @param vo paged execute request
     * @return page result
     */
    @PostMapping("/executeSqlByPage")
    @Operation(summary = "分页执行 SQL（业务库，方言分页 + pageSize clamp [10,500]）")
    public Response011<PageResult011<Map<String, Object>>> executeSqlByPage(
            @RequestBody JulyBusinessModelingPageExecuteVo011 vo) {
        String funcName = "execute sql by page";

        return Response011.success(funcName, useCase.executeSqlByPage(vo.getDataSourceCode(), vo.getSqlContent(),
                vo.getPageIndex(), vo.getPageSize()));
    }
}

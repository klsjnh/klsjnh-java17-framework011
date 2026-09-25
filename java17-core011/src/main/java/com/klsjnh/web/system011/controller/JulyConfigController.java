package com.klsjnh.web.system011.controller;

/*                JulyConfigController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.24
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config controller class
 *      2026.09.15  insert VO extracted to the julyconfig sub-package
 *      2026.09.24  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.system011.config.JulyConfigUseCase;

import com.klsjnh.web.system011.converter.JulyConfigConverter;

import com.klsjnh.web.system011.vo.julyconfig.JulyConfigVo011;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigInsertVo011;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigQueryVo011;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigUpdateVo011;
import com.klsjnh.web.global.audit.AuditLog;
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

/**
 * JulyConfig HTTP adapter: admin CRUD over runtime key-value parameters.
 * There is deliberately NO getByCode HTTP endpoint — config reading is a
 * program behavior (JulyConfigUseCase.getByCode), not a management action.
 * Permission checks live in the use case (operator resolved here).
 */

@Tag(name = "系统管理 - 配置管理")
@RestController
@RequestMapping("/klsjnh/system011/julyConfig/v1")
public class JulyConfigController {

    /**
     * JulyConfig use case.
     */
    private final JulyConfigUseCase julyConfigUseCase;

    /**
     * Response julyConfigConverter.
     */
    private final JulyConfigConverter julyConfigConverter;

    /**
     * Create the controller.
     *
     * @param julyConfigUseCase   july config use case
     * @param julyConfigConverter response julyConfigConverter
     */
    public JulyConfigController(JulyConfigUseCase julyConfigUseCase, JulyConfigConverter julyConfigConverter) {
        this.julyConfigUseCase = julyConfigUseCase;
        this.julyConfigConverter = julyConfigConverter;
    }

    /**
     * Insert a new config entry.
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new config id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_CONFIG)
    @PostMapping("/insert")
    @Operation(summary = "新增配置（code 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyConfigInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyConfigUseCase.insert(operator.id(), vo.getCode(), vo.getData(),
                vo.getStatus(), vo.getRemark()));
    }

    /**
     * Update the value of a config entry (code immutable).
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the config id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_CONFIG)
    @PostMapping("/update")
    @Operation(summary = "修改配置值（code 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyConfigUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyConfigUseCase.update(operator.id(), vo.getId(), vo.getData(), vo.getStatus(), vo.getRemark());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a config entry.
     *
     * @param idVo    request with the config id
     * @param request http request
     * @return envelope with the config id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_CONFIG)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyConfigUseCase.logicDelete(operator.id(), idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Find a config entry by primary key (safe + idempotent, hence GET).
     *
     * @param id      config id, passed as a query parameter
     * @param request http request
     * @return config detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyConfigVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName,
                julyConfigConverter.toVo(julyConfigUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param vo      page query request
     * @param request http request
     * @return page result of config rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（code/data 模糊过滤）")
    public Response011<PageResult011<JulyConfigVo011>> selectListByPage(@RequestBody JulyConfigQueryVo011 vo,
            HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyConfig> page = julyConfigUseCase.selectListByPage(operator.id(), pageQuery, vo.getKeyword(),
                vo.getStatus());

        return Response011.success(funcName, page.withRows(julyConfigConverter.toVoList(page.rows())));
    }

    /**
     * Export every config row in batches and return the whole result in the
     * JSON envelope (batching bounds the database load, not the payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部配置（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyConfigUseCase.export(operator));
    }

    /**
     * Back every config row up into the storage center, keyed by timestamp.
     * Success returns the storage object key only (no config row payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the stored object key
     */
    @PostMapping("/backup011")
    @Operation(summary = "备份全部配置到存储中心（写 BACKUP 审计）")
    public Response011<String> backup011(HttpServletRequest request) {
        String funcName = "backup";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyConfigUseCase.backup(operator));
    }
}

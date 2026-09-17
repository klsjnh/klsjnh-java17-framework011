package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.lowcode011.JulyMetadataUseCase;
import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataQuerySpec;

import com.klsjnh.web.lowcode011.converter.JulyMetadataConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.lowcode011.vo.JulyMetadataQueryVo011;
import com.klsjnh.web.lowcode011.vo.JulyMetadataSaveVo011;
import com.klsjnh.web.lowcode011.vo.JulyMetadataVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyMetadata HTTP adapter: low-code object metadata CRUD (master + three
 * child collections).
 */

@Tag(name = "低代码011 - 对象元数据")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataController {

    /**
     * JulyMetadata use case.
     */
    private final JulyMetadataUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyMetadataConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july metadata use case
     * @param converter response converter
     */
    public JulyMetadataController(JulyMetadataUseCase useCase, JulyMetadataConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new object with its children.
     *
     * @param vo save request
     * @return envelope with the new object id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = "julyMetadata")
    @PostMapping("/insert")
    @Operation(summary = "新增对象元数据（一主三子，整体提交）")
    public Response011<IdVo011> insert(@RequestBody JulyMetadataSaveVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getObjectName(), vo.getSortOrder(),
                vo.getObjectType(), vo.getDescription(), vo.getBusinessField(), vo.getPackageName(),
                vo.getRouterPath(), vo.getRemark(), converter.toFields(vo.getFields()),
                converter.toDisplays(vo.getDisplays()), converter.toServices(vo.getServices())));
    }

    /**
     * Update an object (objectName immutable; children replaced).
     *
     * @param vo save request
     * @return envelope with the object id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/update")
    @Operation(summary = "修改对象元数据（对象名不可变；三子整体替换）")
    public Response011<IdVo011> update(@RequestBody JulyMetadataSaveVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, useCase.update(vo.getId(), vo.getObjectType(), vo.getDescription(),
                vo.getBusinessField(), vo.getPackageName(), vo.getRouterPath(), vo.getSortOrder(), vo.getStatus(),
                vo.getRemark(), converter.toFields(vo.getFields()), converter.toDisplays(vo.getDisplays()),
                converter.toServices(vo.getServices())));
    }

    /**
     * Logic delete an object and its children.
     *
     * @param idVo request with the object id
     * @return envelope with the deleted object id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyMetadata")
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（级联三子，单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, useCase.logicDelete(idVo.getId()));
    }

    /**
     * Logic delete objects in batch.
     *
     * @param idsVo request with the object ids
     * @return per-id summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyMetadata")
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除（级联三子，批量）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, useCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find an object by primary key with its children (GET).
     *
     * @param id object id, passed as a query parameter
     * @return object detail with children
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query，含三子）")
    public Response011<JulyMetadataVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
    }

    /**
     * Find an object by object name with its children (GET).
     *
     * @param objectName object name, passed as a query parameter
     * @return object detail with children
     */
    @GetMapping("/getByObjectName")
    @Operation(summary = "按对象名点查（objectName 走 query，含三子）")
    public Response011<JulyMetadataVo011> getByObjectName(@RequestParam("objectName") String objectName) {
        String funcName = "get by object name";

        return Response011.success(funcName, converter.toVo(useCase.getByObjectName(objectName)));
    }

    /**
     * Page query of object metadata.
     *
     * @param vo page query request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（对象名关键字 + 类型 + 状态）")
    public Response011<PageResult011<JulyMetadataVo011>> selectListByPage(@RequestBody JulyMetadataQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyMetadata> page = useCase.selectListByPage(pageQuery,
                new JulyMetadataQuerySpec(vo.getKeyword(), vo.getObjectType(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }
}

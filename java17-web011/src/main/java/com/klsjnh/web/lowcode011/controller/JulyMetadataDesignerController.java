package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataDesignerController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata designer controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataDesignerUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-code designer HTTP adapter — the read-only phase of the designer: list
 * models, load a model as MetaDTO, save it back, and preview the publish DDL.
 */

@Tag(name = "低代码 - 元数据设计器")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataDesignerController {

    /**
     * Designer use case.
     */
    private final JulyMetadataDesignerUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase designer use case
     */
    public JulyMetadataDesignerController(JulyMetadataDesignerUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * List all models with a display status (GET).
     *
     * @return model rows
     */
    @GetMapping("/listModels")
    @Operation(summary = "模型列表（objectName + 状态）")
    public Response011<List<Map<String, Object>>> listModels() {
        String funcName = "list models";

        return Response011.success(funcName, useCase.listModels());
    }

    /**
     * Load a model as the legacy MetaDTO shape (GET).
     *
     * @param objectName object name, query parameter
     * @return MetaDTO map
     */
    @GetMapping("/load")
    @Operation(summary = "载入模型（MetaDTO：metaData + fieldData + displayData + serviceData）")
    public Response011<Map<String, Object>> load(@RequestParam("objectName") String objectName) {
        String funcName = "load model";

        return Response011.success(funcName, useCase.load(objectName));
    }

    /**
     * Save a model from the MetaDTO shape (insert or update by object name).
     *
     * @param body MetaDTO body
     * @return object id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/save")
    @Operation(summary = "保存模型（objectName 不可变；一主三子整替）")
    public Response011<String> save(@RequestBody Map<String, Object> body) {
        String funcName = "save model";

        return Response011.success(funcName, useCase.save(body, "designer"));
    }

    /**
     * Preview the publish DDL of a model (GET).
     *
     * @param objectName object name, query parameter
     * @return map with the ddl text
     */
    @GetMapping("/previewDdl")
    @Operation(summary = "预览建表 DDL（与发布共用同一生成器）")
    public Response011<Map<String, Object>> previewDdl(@RequestParam("objectName") String objectName) {
        String funcName = "preview ddl";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ddl", useCase.previewDdl(objectName));

        return Response011.success(funcName, result);
    }
}

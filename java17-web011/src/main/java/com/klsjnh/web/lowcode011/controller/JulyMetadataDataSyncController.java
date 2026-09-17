package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataDataSyncController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata data sync controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataDataSyncUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Low-code data sync HTTP adapter — pages a source SQL and upserts the rows
 * into the published physical table.
 */

@Tag(name = "低代码 - 数据同步")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataDataSyncController {

    /**
     * Data sync use case.
     */
    private final JulyMetadataDataSyncUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase data sync use case
     */
    public JulyMetadataDataSyncController(JulyMetadataDataSyncUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Import one page of source data.
     *
     * @param body import request (objectName, dataSourceCode, sqlCode, pageNum, pageSize, forceInit)
     * @return import result
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/importDataFromSql")
    @Operation(summary = "分页导入数据（源 SQL 分页读 → 目标物理表 upsert）")
    public Response011<Map<String, Object>> importDataFromSql(@RequestBody Map<String, Object> body) {
        String funcName = "import data from sql";

        String objectName = text(body.get("objectName"));

        if (objectName == null || objectName.isBlank()) {
            throw BusinessException.badRequest("objectName required");
        }

        return Response011.success(funcName, useCase.importDataFromSql(objectName, text(body.get("dataSourceCode")),
                text(body.get("sqlCode")), integer(body.get("pageNum")), integer(body.get("pageSize")),
                Boolean.TRUE.equals(body.get("forceInit"))));
    }

    /**
     * Import status of an object (GET).
     *
     * @param objectName object name, query parameter
     * @return import status
     */
    @GetMapping("/importStatus")
    @Operation(summary = "数据导入状态（dataInitialized / physicalTable）")
    public Response011<Map<String, Object>> importStatus(@RequestParam("objectName") String objectName) {
        String funcName = "import status";

        return Response011.success(funcName, useCase.importStatus(objectName));
    }

    /**
     * Read a string body value.
     *
     * @param value raw value
     * @return string or null
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Read an int body value.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}

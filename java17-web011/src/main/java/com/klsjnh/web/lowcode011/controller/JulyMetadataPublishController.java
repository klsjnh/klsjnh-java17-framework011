package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataPublishController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata publish controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataPublishUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Low-code publish HTTP adapter — creates or alters the physical table of an
 * object (gated by the ddl-execute switch).
 */

@Tag(name = "低代码011 - 元数据发布")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataPublishController {

    /**
     * Publish use case.
     */
    private final JulyMetadataPublishUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase publish use case
     */
    public JulyMetadataPublishController(JulyMetadataPublishUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Publish an object (create table first time, ALTER afterwards).
     *
     * @param body publish request (objectName, migrateData, includeDeleted)
     * @return publish result
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/publish")
    @Operation(summary = "发布建表（首次 CREATE IF NOT EXISTS；之后仅 ADD COLUMN；禁 DROP）")
    public Response011<Map<String, Object>> publish(@RequestBody Map<String, Object> body) {
        String funcName = "publish";

        String objectName = body.get("objectName") == null ? null : String.valueOf(body.get("objectName"));

        if (objectName == null || objectName.isBlank()) {
            throw BusinessException.badRequest("objectName required");
        }

        boolean migrateData = Boolean.TRUE.equals(body.get("migrateData"));
        boolean includeDeleted = Boolean.TRUE.equals(body.get("includeDeleted"));

        return Response011.success(funcName, useCase.publish(objectName, migrateData, includeDeleted));
    }
}

package com.klsjnh.web.lowcode011.controller;

/*                ModelingIntakeController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling intake controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.ModelingIntakeUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Modeling intake HTTP adapter — "one engine, N gates": review or create an
 * object from a source kind (sql / template / copy / modeling / ai). The
 * downstream review / publish is the same for every kind.
 */

@Tag(name = "低代码011 - 多入口建模")
@RestController
@RequestMapping("/klsjnh/lowcode011/intake/v1")
public class ModelingIntakeController {

    /**
     * Intake use case.
     */
    private final ModelingIntakeUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase intake use case
     */
    public ModelingIntakeController(ModelingIntakeUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Review an intake request (validate + preview, nothing persisted).
     *
     * @param body intake request
     * @return review report
     */
    @PostMapping("/review")
    @Operation(summary = "多入口建模 - 审核（零副作用）：kind=sql/template/copy/modeling/ai")
    public Response011<Map<String, Object>> review(@RequestBody Map<String, Object> body) {
        String funcName = "review";

        return Response011.success(funcName, useCase.review(body));
    }

    /**
     * Create an object from an intake request (save + publish).
     *
     * @param body intake request
     * @return create result
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = "julyMetadataIntake")
    @PostMapping("/create")
    @Operation(summary = "多入口建模 - 创建（落库 + 发布）：kind=sql/template/copy/modeling/ai")
    public Response011<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String funcName = "create";

        return Response011.success(funcName, useCase.create(body));
    }
}

package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataRuntimeMenuController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  runtime menu controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.runtime.JulyMetadataRuntimeUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Runtime menu HTTP adapter: publish a published object as a runtime menu and
 * list runtime menus.
 */

@Tag(name = "低代码011 - 运行时菜单")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataRuntimeMenuController {

    /**
     * Runtime use case.
     */
    private final JulyMetadataRuntimeUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase runtime use case
     */
    public JulyMetadataRuntimeMenuController(JulyMetadataRuntimeUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * List runtime menus.
     *
     * @return runtime menu rows
     */
    @GetMapping("/listRuntimeMenus")
    @Operation(summary = "运行时菜单列表（路由在 /runtime/ 下的已发布对象）")
    public Response011<List<Map<String, Object>>> listRuntimeMenus() {
        String funcName = "list runtime menus";

        return Response011.success(funcName, useCase.listRuntimeMenus());
    }

    /**
     * Publish a runtime menu for a published object (idempotent).
     *
     * @param body request with objectName + optional parentMenuCode / projectCode
     * @return menu entry
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/publishMenu")
    @Operation(summary = "发布运行时菜单（rt_<object> → /runtime/<object>，幂等）")
    public Response011<Map<String, Object>> publishMenu(@RequestBody Map<String, Object> body) {
        String funcName = "publish menu";

        Object objectName = body.get("objectName");

        if (objectName == null || String.valueOf(objectName).isBlank()) {
            throw BusinessException.badRequest("objectName required");
        }

        String parentMenuCode = body.get("parentMenuCode") == null ? null
                : String.valueOf(body.get("parentMenuCode"));
        String projectCode = body.get("projectCode") == null ? null : String.valueOf(body.get("projectCode"));

        return Response011.success(funcName,
                useCase.publishMenu(String.valueOf(objectName), parentMenuCode, projectCode));
    }
}

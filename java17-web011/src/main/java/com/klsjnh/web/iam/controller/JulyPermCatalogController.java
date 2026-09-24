package com.klsjnh.web.iam.controller;

/*                JulyPermCatalogController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission catalog list endpoints
 *
 */

import com.klsjnh.common.response.Response011;

import com.klsjnh.application.iam.perm.JulyPermCatalogUseCase;
import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Permission catalog HTTP adapter: list objects and actions (P3).
 */

@Tag(name = "IAM - 权限目录")
@RestController
@RequestMapping("/klsjnh/iam/julyPermCatalog/v1")
public class JulyPermCatalogController {

    /**
     * Catalog use case.
     */
    private final JulyPermCatalogUseCase catalogUseCase;

    /**
     * Create the controller.
     *
     * @param catalogUseCase catalog use case
     */
    public JulyPermCatalogController(JulyPermCatalogUseCase catalogUseCase) {
        this.catalogUseCase = catalogUseCase;
    }

    /**
     * Select enabled permission objects.
     *
     * @return object rows
     */
    @GetMapping("/selectObjects")
    @Operation(summary = "权限对象列表")
    public Response011<List<Map<String, Object>>> selectObjects() {
        String funcName = "select objects";
        List<Map<String, Object>> rows = catalogUseCase.listObjects().stream().map(this::toObjectMap).toList();

        return Response011.success(funcName, rows);
    }

    /**
     * Select enabled actions under one object.
     *
     * @param objectCode object code
     * @return action rows
     */
    @GetMapping("/selectActions")
    @Operation(summary = "对象下动作列表")
    public Response011<List<Map<String, Object>>> selectActions(@RequestParam("objectCode") String objectCode) {
        String funcName = "select actions";
        List<Map<String, Object>> rows = catalogUseCase.listActions(objectCode).stream().map(this::toActionMap)
                .toList();

        return Response011.success(funcName, rows);
    }

    /**
     * Object → map.
     *
     * @param object aggregate
     * @return map
     */
    private Map<String, Object> toObjectMap(JulyPermObject object) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", object.id().value());
        map.put("objectCode", object.objectCode());
        map.put("objectName", object.objectName());
        map.put("moduleCode", object.moduleCode());
        map.put("sortOrder", object.sortOrder());

        return map;
    }

    /**
     * Action → map.
     *
     * @param action aggregate
     * @return map
     */
    private Map<String, Object> toActionMap(JulyPermAction action) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", action.id().value());
        map.put("objectCode", action.objectCode());
        map.put("actionCode", action.actionCode());
        map.put("actionName", action.actionName());
        map.put("permissionCode", action.permissionCode());
        map.put("sortOrder", action.sortOrder());

        return map;
    }
}

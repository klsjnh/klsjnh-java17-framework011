package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataRuntimeController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  runtime data controller class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataRuntimeUseCase;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime data HTTP adapter (internal, JWT): dynamic meta + CRUD over a
 * published object's physical table, addressed by objectName (query / body, no
 * path variable).
 */

@Tag(name = "低代码011 - 运行时数据")
@RestController
@RequestMapping("/klsjnh/lowcode011/runtime/v1")
public class JulyMetadataRuntimeController {

    /**
     * Runtime use case.
     */
    private final JulyMetadataRuntimeUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase runtime use case
     */
    public JulyMetadataRuntimeController(JulyMetadataRuntimeUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Object metadata (MetaDTO) for the runtime form.
     *
     * @param objectName object name, query parameter
     * @return MetaDTO
     */
    @GetMapping("/meta")
    @Operation(summary = "运行时·对象元信息（MetaDTO）")
    public Response011<Map<String, Object>> meta(@RequestParam("objectName") String objectName) {
        String funcName = "runtime meta";

        return Response011.success(funcName, useCase.meta(objectName));
    }

    /**
     * Page rows.
     *
     * @param body body with objectName + filters / pageIndex / pageSize
     * @return page result
     */
    @PostMapping("/query")
    @Operation(summary = "运行时·分页查询")
    public Response011<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        String funcName = "runtime query";

        return Response011.success(funcName, useCase.query(objectName(body), body));
    }

    /**
     * Insert one row.
     *
     * @param body body with objectName + values
     * @return affected rows
     */
    @PostMapping("/insert")
    @Operation(summary = "运行时·新增")
    public Response011<Map<String, Object>> insert(@RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String funcName = "runtime insert";

        return Response011.success(funcName,
                affected(useCase.create(objectName(body), body, Operator011Resolver.resolve(request))));
    }

    /**
     * Update one row.
     *
     * @param body body with objectName + id/sid + values
     * @return affected rows
     */
    @PostMapping("/update")
    @Operation(summary = "运行时·修改（按 id/sid）")
    public Response011<Map<String, Object>> update(@RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String funcName = "runtime update";

        return Response011.success(funcName,
                affected(useCase.update(objectName(body), body, Operator011Resolver.resolve(request))));
    }

    /**
     * Delete one row (logic delete when supported).
     *
     * @param body body with objectName + id/sid
     * @return affected rows
     */
    @PostMapping("/delete")
    @Operation(summary = "运行时·删除（逻辑删）")
    public Response011<Map<String, Object>> delete(@RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String funcName = "runtime delete";

        return Response011.success(funcName,
                affected(useCase.delete(objectName(body), body, Operator011Resolver.resolve(request))));
    }

    /**
     * Extract the object name from the body.
     *
     * @param body request body
     * @return object name
     */
    private String objectName(Map<String, Object> body) {
        Object value = body == null ? null : body.get("objectName");

        if (value == null || String.valueOf(value).isBlank()) {
            throw BusinessException.badRequest("objectName required");
        }

        return String.valueOf(value);
    }

    /**
     * Wrap an affected-row count.
     *
     * @param rows affected rows
     * @return result map
     */
    private Map<String, Object> affected(int rows) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("affected", rows);

        return result;
    }
}

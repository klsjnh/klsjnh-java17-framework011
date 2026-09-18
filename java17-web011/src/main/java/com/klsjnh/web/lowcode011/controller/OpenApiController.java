package com.klsjnh.web.lowcode011.controller;

/*                OpenApiController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api public controller class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.ObjectQueryCommand;
import com.klsjnh.application.lowcode011.runtime.OpenApiUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public open API adapter ({@code /klsjnh/open/v1}): api_key authenticated
 * metadata read plus CRUD on a published object's physical table. JWT is not
 * required (the global filter whitelists this prefix). The object name travels
 * as a query parameter or inside the JSON body (path variables are forbidden by
 * the project URL rules).
 */

@Tag(name = "低代码011 - 开放 API")
@RestController
@RequestMapping("/klsjnh/open/v1")
public class OpenApiController {

    /**
     * Api key header name.
     */
    private static final String API_KEY_HEADER = "X-Api-Key";

    /**
     * Open API use case.
     */
    private final OpenApiUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase open api use case
     */
    public OpenApiController(OpenApiUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Object metadata (MetaDTO).
     *
     * @param objectName object name, query parameter
     * @param apiKey     api key header
     * @return MetaDTO
     */
    @GetMapping("/getMeta")
    @Operation(summary = "对象元信息（MetaDTO，api_key 鉴权）")
    public Response011<Map<String, Object>> getMeta(@RequestParam("objectName") String objectName,
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {
        String funcName = "open meta";

        return Response011.success(funcName, useCase.getMeta(objectName, apiKey));
    }

    /**
     * Page rows.
     *
     * @param apiKey api key header
     * @param body   query body (objectName, filters, pageIndex, pageSize)
     * @return page result
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询（api_key 鉴权）")
    public Response011<Map<String, Object>> query(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, Object> body) {
        String funcName = "open query";

        return Response011.success(funcName, useCase.query(objectName(body), apiKey, command(body)));
    }

    /**
     * Build the typed query command from the request body.
     *
     * @param body request body
     * @return query command
     */
    @SuppressWarnings("unchecked")
    private ObjectQueryCommand command(Map<String, Object> body) {
        Object filters = body.get("filters");
        Map<String, Object> where = filters instanceof Map ? (Map<String, Object>) filters : Map.of();

        return new ObjectQueryCommand(objectName(body), where, integer(body.get("pageIndex")),
                integer(body.get("pageSize")));
    }

    /**
     * Read an optional int body value.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * Insert one row.
     *
     * @param apiKey api key header
     * @param body   row values (objectName + columns)
     * @return affected rows
     */
    @PostMapping("/insert")
    @Operation(summary = "新增（api_key 鉴权）")
    public Response011<Map<String, Object>> insert(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, Object> body) {
        String funcName = "open insert";

        return Response011.success(funcName, affected(useCase.create(objectName(body), apiKey, body)));
    }

    /**
     * Update one row.
     *
     * @param apiKey api key header
     * @param body   objectName + id/sid + values
     * @return affected rows
     */
    @PostMapping("/update")
    @Operation(summary = "修改（按 id/sid；api_key 鉴权）")
    public Response011<Map<String, Object>> update(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, Object> body) {
        String funcName = "open update";

        return Response011.success(funcName, affected(useCase.update(objectName(body), apiKey, body)));
    }

    /**
     * Delete one row (logic delete when supported).
     *
     * @param apiKey api key header
     * @param body   objectName + id/sid
     * @return affected rows
     */
    @PostMapping("/delete")
    @Operation(summary = "删除（逻辑删；api_key 鉴权）")
    public Response011<Map<String, Object>> delete(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, Object> body) {
        String funcName = "open delete";

        return Response011.success(funcName, affected(useCase.delete(objectName(body), apiKey, body)));
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

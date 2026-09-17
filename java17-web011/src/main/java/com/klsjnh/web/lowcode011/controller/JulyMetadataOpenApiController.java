package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataOpenApiController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api config controller class
 *
 */

import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataOpenApiUseCase;

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
 * Open API configuration HTTP adapter (admin side): read, save and rotate the
 * api key of a low-code object.
 */

@Tag(name = "低代码011 - 开放 API 配置")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataOpenApiController {

    /**
     * Open API config use case.
     */
    private final JulyMetadataOpenApiUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase open api config use case
     */
    public JulyMetadataOpenApiController(JulyMetadataOpenApiUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Read the open API config (GET; api key masked).
     *
     * @param objectName object name, query parameter
     * @return config view
     */
    @GetMapping("/openApiConfig")
    @Operation(summary = "开放 API 配置（apiKey 打码）")
    public Response011<Map<String, Object>> getOpenApiConfig(@RequestParam("objectName") String objectName) {
        String funcName = "open api config";

        return Response011.success(funcName, useCase.getConfig(objectName));
    }

    /**
     * Save the open API config (api key returned in clear once when generated).
     *
     * @param body config payload
     * @return config view
     */
    @PostMapping("/saveOpenApiConfig")
    @Operation(summary = "保存开放 API 配置（首次生成 apiKey 时回明文一次）")
    public Response011<Map<String, Object>> saveOpenApiConfig(@RequestBody Map<String, Object> body) {
        String funcName = "save open api config";

        return Response011.success(funcName, useCase.saveConfig(body));
    }

    /**
     * Rotate the api key (returned in clear once).
     *
     * @param body request with objectName
     * @return config view
     */
    @PostMapping("/rotateApiKey")
    @Operation(summary = "轮换 apiKey（回明文一次）")
    public Response011<Map<String, Object>> rotateApiKey(@RequestBody Map<String, Object> body) {
        String funcName = "rotate api key";

        Object objectName = body.get("objectName");

        return Response011.success(funcName,
                useCase.rotateApiKey(objectName == null ? null : String.valueOf(objectName)));
    }
}

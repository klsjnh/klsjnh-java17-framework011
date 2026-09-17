package com.klsjnh.web.lowcode011.controller;

/*                JulyMetadataTemplateController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata template controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.JulyMetadataTemplateUseCase;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Metadata template HTTP adapter: default skeleton, export, upload review
 * (validate + preview, no persistence) and deploy (save + publish). Upload is a
 * single file (multipart) or a raw JSON body.
 */

@Tag(name = "低代码011 - 元数据模板")
@RestController
@RequestMapping("/klsjnh/lowcode011/julyMetadata/v1")
public class JulyMetadataTemplateController {

    /**
     * Metadata template use case.
     */
    private final JulyMetadataTemplateUseCase useCase;

    /**
     * JSON mapper for the multipart body.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the controller.
     *
     * @param useCase metadata template use case
     */
    public JulyMetadataTemplateController(JulyMetadataTemplateUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Default annotated skeleton template.
     *
     * @return skeleton template
     */
    @GetMapping("/getTemplate011")
    @Operation(summary = "取默认模板（合法 JSON，_guide 内为说明）")
    public Response011<Map<String, Object>> getTemplate011() {
        String funcName = "get template";

        return Response011.success(funcName, useCase.blankTemplate());
    }

    /**
     * Export an existing object as a template (1 master + 3 children + source).
     *
     * @param objectName object name, query parameter
     * @return template
     */
    @GetMapping("/downloadTemplate011")
    @Operation(summary = "导出对象模板（1 主 3 子 + source；objectName 走 query）")
    public Response011<Map<String, Object>> downloadTemplate011(@RequestParam("objectName") String objectName) {
        String funcName = "download template";

        return Response011.success(funcName, useCase.downloadTemplate(objectName));
    }

    /**
     * Upload a template file (multipart) and review it (no persistence).
     *
     * @param file template json file
     * @return review report
     * @throws Exception parse failure
     */
    @PostMapping(value = "/uploadTemplate011", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传模板文件并审核（不落库，返回校验 + previewDdl）")
    public Response011<Map<String, Object>> uploadTemplate011(@RequestParam("file") MultipartFile file)
            throws Exception {
        String funcName = "upload template";

        return Response011.success(funcName, useCase.reviewTemplate(parse(file.getBytes())));
    }

    /**
     * Upload a template as a raw JSON body and review it (no persistence).
     *
     * @param template template value
     * @return review report
     */
    @PostMapping(value = "/uploadTemplate011", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "上传模板 JSON 并审核（不落库）")
    public Response011<Map<String, Object>> uploadTemplate011Json(@RequestBody Map<String, Object> template) {
        String funcName = "upload template";

        return Response011.success(funcName, useCase.reviewTemplate(template));
    }

    /**
     * Deploy a template (validate + save + publish) or publish an existing
     * object by name.
     *
     * @param body template ({@code template}) and/or {@code objectName};
     *             optional {@code overwrite}
     * @return deploy result
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyMetadata")
    @PostMapping("/deployObject")
    @Operation(summary = "部署对象（校验 → 落库 → 发布建表/ALTER）")
    @SuppressWarnings("unchecked")
    public Response011<Map<String, Object>> deployObject(@RequestBody Map<String, Object> body) {
        String funcName = "deploy object";

        Map<String, Object> template = body.get("template") instanceof Map
                ? (Map<String, Object>) body.get("template")
                : null;
        String objectName = body.get("objectName") == null ? null : String.valueOf(body.get("objectName"));
        boolean overwrite = Boolean.TRUE.equals(body.get("overwrite"));

        return Response011.success(funcName, useCase.deployObject(template, objectName, overwrite));
    }

    /**
     * Parse an uploaded template file.
     *
     * @param bytes file bytes
     * @return template map
     * @throws Exception parse failure
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(byte[] bytes) throws Exception {
        try {
            return objectMapper.readValue(bytes, Map.class);
        } catch (Exception ex) {
            throw BusinessException.badRequest("template file is not valid json: " + ex.getMessage());
        }
    }
}

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
 *      2026.09.18  multi-format (json/csv/xlsx) export + upload
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.lowcode011.template.JulyMetadataTemplateUseCase;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;
import com.klsjnh.domain.lowcode011.records.IntakeKey011;

import com.klsjnh.web.global.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Metadata template HTTP adapter: skeleton, export, upload review (validate +
 * preview, no persistence) and deploy (save + publish). Export defaults to the
 * JSON envelope; {@code format=csv|xlsx} returns a file stream. Upload accepts
 * a single file (multipart, format by extension) or a raw JSON body.
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
     * Create the controller.
     *
     * @param useCase metadata template use case
     */
    public JulyMetadataTemplateController(JulyMetadataTemplateUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Default skeleton template (JSON envelope, or a file for csv/xlsx).
     *
     * @param format optional format code (json / csv / xlsx)
     * @return envelope or file
     */
    @GetMapping("/getTemplate011")
    @Operation(summary = "取默认模板（默认 JSON；format=csv|xlsx 返回文件流）")
    public ResponseEntity<Object> getTemplate011(@RequestParam(value = "format", required = false) String format) {
        String funcName = "get template";

        if (isFileFormat(format)) {
            return file(useCase.blankFile(format), format, "template011." + extension(format));
        }

        return ResponseEntity.ok(Response011.success(funcName, useCase.blankTemplate()));
    }

    /**
     * Export an existing object as a template (JSON envelope, or a file).
     *
     * @param objectName object name, query parameter
     * @param format     optional format code (json / csv / xlsx)
     * @return envelope or file
     */
    @GetMapping("/downloadTemplate011")
    @Operation(summary = "导出对象模板（默认 JSON；format=csv|xlsx 返回文件流）")
    public ResponseEntity<Object> downloadTemplate011(@RequestParam("objectName") String objectName,
            @RequestParam(value = "format", required = false) String format) {
        String funcName = "download template";

        if (isFileFormat(format)) {
            return file(useCase.downloadFile(objectName, format), format, objectName + "." + extension(format));
        }

        return ResponseEntity.ok(Response011.success(funcName, useCase.downloadTemplate(objectName)));
    }

    /**
     * Upload a template file (multipart, format by extension) and review it.
     *
     * @param file template file (json / csv)
     * @return review report
     * @throws Exception read failure
     */
    @PostMapping(value = "/uploadTemplate011", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传模板文件并审核（json/csv 按扩展名识别；不落库）")
    public Response011<Map<String, Object>> uploadTemplate011(@RequestParam("file") MultipartFile file)
            throws Exception {
        String funcName = "upload template";

        Map<String, Object> template = useCase.parseFile(file.getBytes(), file.getOriginalFilename(), null);

        return Response011.success(funcName, useCase.reviewTemplate(template));
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

        Map<String, Object> template = body.get(IntakeKey011.TEMPLATE) instanceof Map
                ? (Map<String, Object>) body.get(IntakeKey011.TEMPLATE)
                : null;
        String objectName = body.get(IntakeKey011.OBJECT_NAME) == null ? null
                : String.valueOf(body.get(IntakeKey011.OBJECT_NAME));
        boolean overwrite = Boolean.TRUE.equals(body.get(IntakeKey011.OVERWRITE));

        return Response011.success(funcName, useCase.deployObject(template, objectName, overwrite));
    }

    /**
     * Whether the requested format needs a file stream (anything but json).
     *
     * @param format format code, nullable
     * @return true for csv/xlsx
     */
    private boolean isFileFormat(String format) {
        return format != null && !format.isBlank()
                && !TemplateFormat011.JSON.getCode().equalsIgnoreCase(format.trim());
    }

    /**
     * Normalise a format code for a filename.
     *
     * @param format format code, nullable
     * @return lower-case code, json by default
     */
    private String extension(String format) {
        TemplateFormat011 resolved = TemplateFormat011.fromString(format);

        return (resolved == null ? TemplateFormat011.JSON : resolved).getCode();
    }

    /**
     * Build a file download response.
     *
     * @param body     bytes
     * @param format   format code
     * @param filename download filename
     * @return response entity
     */
    private ResponseEntity<Object> file(byte[] body, String format, String filename) {
        TemplateFormat011 resolved = TemplateFormat011.fromString(format);
        MediaType mediaType;

        if (resolved == TemplateFormat011.CSV) {
            mediaType = new MediaType("text", "csv");
        } else if (resolved == TemplateFormat011.MARKDOWN) {
            mediaType = new MediaType("text", "markdown");
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}

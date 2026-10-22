package com.klsjnh.web.aicenter.controller;

/*                AiFileController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file assets (upload / get / list / delete / content)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.file.AiFileUseCase;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.file.AiFilePurpose;
import com.klsjnh.domain.aicenter.file.AiFileRef;

import com.klsjnh.web.aicenter.converter.AiFileConverter011;

import com.klsjnh.web.aicenter.vo.aifile.AiFileContentVo011;
import com.klsjnh.web.aicenter.vo.aifile.AiFileListVo011;
import com.klsjnh.web.aicenter.vo.aifile.AiFileQueryVo011;
import com.klsjnh.web.aicenter.vo.aifile.AiFileUploadVo011;
import com.klsjnh.web.aicenter.vo.aifile.AiFileVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Base64;
import java.util.List;

/**
 * AI provider file HTTP adapter ({@code /klsjnh/aicenter/julyAiFile/v1}):
 * upload, metadata, list, delete and content. Bytes stay at the provider; the
 * storage center still owns business files.
 */

@Tag(name = "AI中心011 - 厂商文件")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiFile/v1")
public class AiFileController {

    /**
     * File use case.
     */
    private final AiFileUseCase aiFileUseCase;

    /**
     * Converter.
     */
    private final AiFileConverter011 converter;

    /**
     * Create the controller.
     *
     * @param aiFileUseCase file use case
     * @param converter     converter
     */
    public AiFileController(AiFileUseCase aiFileUseCase, AiFileConverter011 converter) {
        this.aiFileUseCase = aiFileUseCase;
        this.converter = converter;
    }

    /**
     * Upload one file.
     *
     * @param vo      request
     * @param request http request
     * @return file metadata
     */
    @PostMapping("/upload")
    @Operation(summary = "上传厂商文件（bytes 或 url；purpose 必传）")
    public Response011<AiFileVo011> upload(@Valid @RequestBody AiFileUploadVo011 vo, HttpServletRequest request) {
        String funcName = "ai file upload";
        Operator011 operator = Operator011Resolver.resolve(request);

        byte[] bytes = StringUtil011.isBlank(vo.getFileBase64()) ? null
                : Base64.getDecoder().decode(vo.getFileBase64());

        AiFileRef ref = aiFileUseCase.upload(operator.id(), target(vo.getProvider(), vo.getProviderId(),
                vo.getApi(), vo.getApiId()), vo.getFileName(), bytes, vo.getUrl(), purpose(vo.getPurpose()));

        return Response011.success(funcName, converter.toVo(ref));
    }

    /**
     * Get one file's metadata.
     *
     * @param vo      request
     * @param request http request
     * @return file metadata
     */
    @PostMapping("/get")
    @Operation(summary = "查询厂商文件信息")
    public Response011<AiFileVo011> get(@Valid @RequestBody AiFileQueryVo011 vo, HttpServletRequest request) {
        String funcName = "ai file get";
        Operator011 operator = Operator011Resolver.resolve(request);

        AiFileRef ref = aiFileUseCase.get(operator.id(), target(vo.getProvider(), vo.getProviderId(), vo.getApi(),
                vo.getApiId()), vo.getFileId());

        return Response011.success(funcName, converter.toVo(ref));
    }

    /**
     * List files.
     *
     * @param vo      request
     * @param request http request
     * @return file metadata list
     */
    @PostMapping("/list")
    @Operation(summary = "厂商文件列表（可按 purpose 过滤）")
    public Response011<List<AiFileVo011>> list(@Valid @RequestBody AiFileListVo011 vo, HttpServletRequest request) {
        String funcName = "ai file list";
        Operator011 operator = Operator011Resolver.resolve(request);
        AiFilePurpose purpose = StringUtil011.isBlank(vo.getPurpose()) ? null : purpose(vo.getPurpose());

        List<AiFileVo011> data = aiFileUseCase.list(operator.id(), target(vo.getProvider(), vo.getProviderId(),
                vo.getApi(), vo.getApiId()), purpose).stream().map(converter::toVo).toList();

        return Response011.success(funcName, data);
    }

    /**
     * Delete one file.
     *
     * @param vo      request
     * @param request http request
     * @return deleted file id
     */
    @PostMapping("/delete")
    @Operation(summary = "删除厂商文件")
    public Response011<String> delete(@Valid @RequestBody AiFileQueryVo011 vo, HttpServletRequest request) {
        String funcName = "ai file delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        aiFileUseCase.delete(operator.id(), target(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId()),
                vo.getFileId());

        return Response011.success(funcName, vo.getFileId());
    }

    /**
     * Download file content (base64).
     *
     * @param vo      request
     * @param request http request
     * @return content
     */
    @PostMapping("/content")
    @Operation(summary = "下载厂商文件内容（base64）")
    public Response011<AiFileContentVo011> content(@Valid @RequestBody AiFileQueryVo011 vo,
            HttpServletRequest request) {
        String funcName = "ai file content";
        Operator011 operator = Operator011Resolver.resolve(request);

        byte[] bytes = aiFileUseCase.content(operator.id(), target(vo.getProvider(), vo.getProviderId(), vo.getApi(),
                vo.getApiId()), vo.getFileId());

        return Response011.success(funcName, converter.toContentVo(vo.getFileId(), bytes));
    }

    /**
     * Build the routing header.
     *
     * @param provider   provider code
     * @param providerId provider id
     * @param api        api code
     * @param apiId      api id
     * @return target
     */
    private AiInvokeTarget target(String provider, String providerId, String api, String apiId) {
        return new AiInvokeTarget(provider, providerId, api, apiId, null, null);
    }

    /**
     * Parse a purpose.
     *
     * @param value wire value
     * @return purpose
     */
    private AiFilePurpose purpose(String value) {
        try {
            return AiFilePurpose.from(value);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }
}

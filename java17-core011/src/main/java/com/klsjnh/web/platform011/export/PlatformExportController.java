package com.klsjnh.web.platform011.export;

/*                PlatformExportController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  generic platform export controller (discovery + streaming
 *                  download in json / xlsx / csv)
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.platform011.export.ExportUseCase;
import com.klsjnh.domain.platform011.export.ExportProviderInfo;

import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generic platform export HTTP adapter ({@code /klsjnh/platform011/export/v1}):
 * discover the registered export providers and download any of them streamed
 * as json / xlsx / csv. The download is NOT the response envelope — it is a
 * streamed file (013 contract exception for export downloads); rows are pulled
 * batch by batch so memory stays bounded. Permission is the per-object export
 * code ({@code module:objectCode:export}), asserted in the use case.
 */

@Tag(name = "平台011 - 数据导出")
@RestController
@RequestMapping("/klsjnh/platform011/export/v1")
public class PlatformExportController {

    /**
     * Response media type per export format.
     */
    private static final java.util.Map<String, MediaType> MEDIA_TYPES = java.util.Map.of(
            "json", MediaType.APPLICATION_JSON,
            "csv", MediaType.parseMediaType("text/csv"),
            "xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

    /**
     * Export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Create the controller.
     *
     * @param exportUseCase export use case
     */
    public PlatformExportController(ExportUseCase exportUseCase) {
        this.exportUseCase = exportUseCase;
    }

    /**
     * List the registered export providers.
     *
     * @param request http request
     * @return provider infos
     */
    @PostMapping("/selectProviders")
    @Operation(summary = "查询已注册的导出对象清单（objectCode / moduleCode / 列数）")
    public Response011<List<ExportProviderInfo>> selectProviders(HttpServletRequest request) {
        String funcName = "select export providers";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, exportUseCase.providers(operator));
    }

    /**
     * Download one object's export as a streamed file.
     *
     * @param vo      export request
     * @param request http request
     * @return streamed download
     */
    @PostMapping("/export")
    @Operation(summary = "流式导出（json / xlsx / csv 下载，逐批拉取，内存有界；需对象 export 权限码）")
    public ResponseEntity<StreamingResponseBody> export(@Valid @RequestBody PlatformExportVo011 vo,
            HttpServletRequest request) {
        String funcName = "platform export";
        Operator011 operator = Operator011Resolver.resolve(request);

        Consumer<java.io.OutputStream> stream = exportUseCase.stream(vo.getFormat(), vo.getObjectCode(), operator);

        String filename = vo.getObjectCode() + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "." + vo.getFormat();

        StreamingResponseBody body = stream::accept;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MEDIA_TYPES.get(vo.getFormat()))
                .body(body);
    }
}

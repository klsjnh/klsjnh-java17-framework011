package com.klsjnh.web.system011.controller;

/*                ExportController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export controller class
 *
 */

import lombok.Data;

import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.export.ExportUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Platform data export HTTP adapter: binary download (bypasses the JSON
 * envelope by design) — requires an authenticated operator.
 */

@Tag(name = "数据导出")
@RestController
@RequestMapping("/klsjnh/system011/export/v1")
public class ExportController {

    /**
     * Export use case.
     */
    private final ExportUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase export use case
     */
    public ExportController(ExportUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Export an object's rows as a downloadable file.
     *
     * @param vo      export request
     * @param request http request (operator id from the auth filter)
     * @return binary file response
     */
    @PostMapping
    @Operation(summary = "数据导出（json / csv，上限 500 行，写 EXPORT 审计）")
    public ResponseEntity<byte[]> export(@RequestBody ExportRequestVo vo, HttpServletRequest request) {
        String funcName = "export";

        String operatorId = (String) request.getAttribute(FrameConst011.OPERATOR_ID);

        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ExportUseCase.ExportFile file = useCase.export(vo.getObjectCode(), vo.getFormat(), operatorId,
                vo.getObjectCode(), request.getRemoteAddr());

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }

    /**
     * Export request VO.
     */
    @Data
    public static class ExportRequestVo {

        /** Object code of a registered export provider (e.g. julyUser). */
        @Schema(description = "导出对象编码（已注册的 provider，如 julyUser）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String objectCode;

        /** Export format: json / csv. */
        @Schema(description = "导出格式（json / csv）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String format;
    }
}

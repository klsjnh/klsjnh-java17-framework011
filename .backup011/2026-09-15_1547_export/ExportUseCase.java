package com.klsjnh.application.export;

/*                ExportUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export use case class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.ExportFormat011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.UserAuditPort;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Platform data export use case: resolves the object's registered provider,
 * serializes rows to the requested format (JSON / CSV) and writes an EXPORT
 * audit row. Export requires an authenticated operator and is capped at 500
 * rows (product decision, code-enforced).
 */

@Service
public class ExportUseCase {

    /**
     * Export row cap (product decision, code-enforced).
     */
    private static final int MAX_ROWS = 500;

    /**
     * CSV UTF-8 BOM so Excel opens the file with the right encoding.
     */
    private static final String CSV_BOM = "\uFEFF";

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExportUseCase.class);

    /**
     * Provider registry.
     */
    private final ExportProviderRegistry registry;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param registry       export provider registry
     * @param userAuditPort  user audit port
     */
    public ExportUseCase(ExportProviderRegistry registry, UserAuditPort userAuditPort) {
        this.registry = registry;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Export the object's rows in the requested format.
     *
     * @param objectCode     object code (registered provider)
     * @param format         export format (json / csv)
     * @param operatorId     current operator id
     * @param operatorAccount current operator account
     * @param ip             client IP
     * @return exported file (bytes + download name + content type)
     */
    public ExportFile export(String objectCode, String format, String operatorId, String operatorAccount, String ip) {
        String funcName = "export";

        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ExportProvider provider = registry.get(objectCode);

        if (provider == null) {
            throw BusinessException.badRequest(funcName + ": unknown export object " + objectCode);
        }

        ExportFormat011 fmt = ExportFormat011.of(format);

        if (fmt == null) {
            throw BusinessException.badRequest(funcName + ": unknown export format " + format);
        }

        List<Map<String, Object>> rows = provider.exportRows();
        int total = rows.size();

        if (total > MAX_ROWS) {
            rows = rows.subList(0, MAX_ROWS);
            logger.warn("{} {} truncated {} to {} rows ...", funcName, objectCode, total, MAX_ROWS);
        }

        byte[] bytes = fmt == ExportFormat011.CSV ? toCsv(rows) : toJson(rows);
        String stamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String filename = objectCode + "_" + stamp + "." + fmt.getCode();

        userAuditPort.record(operatorId, operatorAccount, "EXPORT", objectCode,
                "export " + rows.size() + " of " + total + " rows", ip);

        return new ExportFile(filename, bytes, contentTypeOf(fmt));
    }

    /**
     * Serialize rows as pretty JSON.
     *
     * @param rows rows
     * @return json bytes
     */
    private byte[] toJson(List<Map<String, Object>> rows) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(rows);
        } catch (IOException ex) {
            throw new IllegalStateException("export serialize failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Serialize rows as CSV (UTF-8 BOM + header + escaped values).
     *
     * @param rows rows
     * @return csv bytes
     */
    private byte[] toCsv(List<Map<String, Object>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(CSV_BOM.getBytes(StandardCharsets.UTF_8));

        if (rows.isEmpty()) {
            return out.toByteArray();
        }

        List<String> headers = List.copyOf(rows.get(0).keySet());
        StringBuilder sb = new StringBuilder();

        sb.append(String.join(",", headers.stream().map(this::escape).toList())).append("\r\n");

        for (Map<String, Object> row : rows) {
            sb.append(String.join(",", headers.stream()
                    .map(h -> escape(row.get(h) == null ? "" : String.valueOf(row.get(h))))
                    .toList())).append("\r\n");
        }

        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }

    /**
     * Escape one CSV cell: wrap in quotes when needed, double inner quotes.
     *
     * @param value cell value
     * @return escaped cell
     */
    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Content type of a format.
     *
     * @param format export format
     * @return mime type
     */
    private String contentTypeOf(ExportFormat011 format) {
        return format == ExportFormat011.CSV ? "text/csv" : "application/json";
    }

    /**
     * Exported file result.
     *
     * @param filename    download file name
     * @param bytes       file content
     * @param contentType mime type
     */
    public record ExportFile(String filename, byte[] bytes, String contentType) {
    }
}

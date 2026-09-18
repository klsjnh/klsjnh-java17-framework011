package com.klsjnh.application.lowcode011;

/*                OpenApiUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api public use case class
 *      2026.09.17  delegate data plane to ObjectTableGateway; apiKey NPE -> 401
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApi;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApiRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Public open API use case: api_key authenticated CRUD plus the metadata read
 * over a published object's physical table. The data plane is delegated to
 * {@link ObjectTableGateway}; this class keeps only the open API authorization,
 * metadata read and the audit.
 */

@Service
public class OpenApiUseCase {

    /**
     * Configuration repository.
     */
    private final JulyMetadataOpenApiRepository configRepository;

    /**
     * Designer use case (metadata read).
     */
    private final JulyMetadataDesignerUseCase designerUseCase;

    /**
     * User audit port (dynamic objectCode for open API writes).
     */
    private final UserAuditPort userAuditPort;

    /**
     * Object table kernel.
     */
    private final ObjectTableGateway tableGateway;

    /**
     * Create the use case.
     *
     * @param configRepository configuration repository
     * @param designerUseCase  designer use case
     * @param userAuditPort    user audit port
     * @param tableGateway     object table gateway
     */
    public OpenApiUseCase(JulyMetadataOpenApiRepository configRepository, JulyMetadataDesignerUseCase designerUseCase,
            UserAuditPort userAuditPort, ObjectTableGateway tableGateway) {
        this.configRepository = configRepository;
        this.designerUseCase = designerUseCase;
        this.userAuditPort = userAuditPort;
        this.tableGateway = tableGateway;
    }

    /**
     * Read the object metadata (MetaDTO).
     *
     * @param objectName object name
     * @param apiKey     api key, nullable for authMode none
     * @return MetaDTO
     */
    public Map<String, Object> getMeta(String objectName, String apiKey) {
        authorize(objectName, apiKey, "query");

        return designerUseCase.load(objectName);
    }

    /**
     * Page rows of a published object.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       query body (filters, pageIndex, pageSize)
     * @return page result
     */
    public Map<String, Object> query(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "query");

        Object filters = body.get("filters");

        @SuppressWarnings("unchecked")
        Map<String, Object> where = filters instanceof Map ? (Map<String, Object>) filters : Map.of();

        return tableGateway.query(objectName, where, integer(body.get("pageIndex")), integer(body.get("pageSize")));
    }

    /**
     * Insert one row.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       row values
     * @return affected rows
     */
    public int create(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "insert");

        int rows = tableGateway.insert(objectName, body);
        audit(objectName, AuditType011.INSERT);

        return rows;
    }

    /**
     * Update one row by id (or the given business key).
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       key + values
     * @return affected rows
     */
    public int update(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "update");

        int rows = tableGateway.update(objectName, body);
        audit(objectName, AuditType011.UPDATE);

        return rows;
    }

    /**
     * Delete one row by id (or the given business key); logic delete when the
     * table carries a {@code dr}.
     *
     * @param objectName object name
     * @param apiKey     api key
     * @param body       key
     * @return affected rows
     */
    public int delete(String objectName, String apiKey, Map<String, Object> body) {
        authorize(objectName, apiKey, "delete");

        int rows = tableGateway.delete(objectName, body);
        audit(objectName, AuditType011.DELETE);

        return rows;
    }

    /**
     * Authorize an open API call: enabled + auth mode + allowed operation. A
     * missing key under {@code apiKey} auth is a 401, never an NPE.
     *
     * @param objectName object name
     * @param apiKey     api key, nullable
     * @param op         operation (query / insert / update / delete)
     */
    private void authorize(String objectName, String apiKey, String op) {
        JulyMetadataOpenApi config = configRepository.findByObjectName(objectName);

        if (config == null || !config.enabled()) {
            throw BusinessException.unauthorized("open api not enabled: " + objectName);
        }

        if ("apiKey".equals(config.authMode())
                && (apiKey == null || !apiKey.equals(config.apiKey()))) {
            throw BusinessException.unauthorized("invalid apiKey");
        }

        if (!allowOps(config.allowedOps()).contains(op)) {
            throw BusinessException.unauthorized("operation not allowed: " + op);
        }
    }

    /**
     * Parse the allowed operations.
     *
     * @param ops comma list
     * @return operations
     */
    private List<String> allowOps(String ops) {
        return ops == null || ops.isBlank() ? List.of() : List.of(ops.split(","));
    }

    /**
     * Record an audit row with the dynamic object code; never breaks the write.
     *
     * @param objectName object name
     * @param type       audit type
     */
    private void audit(String objectName, AuditType011 type) {
        try {
            userAuditPort.record(null, "open-api", type, objectName,
                    "open-api " + type.name().toLowerCase(Locale.ROOT) + " " + objectName, null);
        } catch (Exception ignored) {
            // audit must never break the business write
        }
    }

    /**
     * Read a positive int with a null default.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}

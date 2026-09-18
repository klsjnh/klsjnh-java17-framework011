package com.klsjnh.application.lowcode011;

/*                JulyMetadataOpenApiUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api config use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.domain.lowcode011.JulyMetadataOpenApi;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApiRepository;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Open API configuration use case (admin side): read the config, save it and
 * rotate the api key. The key is masked on every output except the save /
 * rotate call that generates it (returned in clear exactly once).
 */

@Service
public class JulyMetadataOpenApiUseCase {

    /**
     * Open API base path prefix.
     */
    private static final String BASE_PATH = "/klsjnh/open/v1/";

    /**
     * Hex characters for key generation.
     */
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * Secure random for key generation.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Configuration repository.
     */
    private final JulyMetadataOpenApiRepository repository;

    /**
     * Create the use case.
     *
     * @param repository configuration repository
     */
    public JulyMetadataOpenApiUseCase(JulyMetadataOpenApiRepository repository) {
        this.repository = repository;
    }

    /**
     * Read the configuration of an object (api key masked).
     *
     * @param objectName object name
     * @return configuration view
     */
    public Map<String, Object> getConfig(String objectName) {
        JulyMetadataOpenApi config = repository.findByObjectName(objectName);

        if (config == null) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("objectName", objectName);
            view.put("enabled", false);
            view.put("authMode", "closed");
            view.put("allowedOps", List.of("query"));
            view.put("apiKeyConfigured", false);
            view.put("apiKeyHint", null);
            view.put("openApiBasePath", BASE_PATH + objectName);
            return view;
        }

        return toView(config, null);
    }

    /**
     * Save the configuration. When switching to apiKey auth and no key exists,
     * one is generated and returned in clear exactly once.
     *
     * @param payload configuration payload
     * @return configuration view (may carry the clear api key)
     */
    public Map<String, Object> saveConfig(Map<String, Object> payload) {
        String objectName = text(payload.get("objectName"));

        if (objectName == null || objectName.isBlank()) {
            throw BusinessException.badRequest("objectName required");
        }

        boolean enabled = Boolean.TRUE.equals(payload.get("enabled"));
        String authMode = defaultIfBlank(text(payload.get("authMode")), "closed");
        String allowedOps = joinOps(payload.get("allowedOps"));

        JulyMetadataOpenApi existing = repository.findByObjectName(objectName);
        String apiKey = existing == null ? null : existing.apiKey();
        String clearKey = null;

        if (enabled && "apiKey".equals(authMode) && (apiKey == null || apiKey.isBlank())) {
            clearKey = randomKey();
            apiKey = clearKey;
        }

        JulyMetadataOpenApi config = new JulyMetadataOpenApi(existing == null ? EntityId.generate().value()
                : existing.id(), objectName, enabled, authMode, allowedOps, apiKey,
                clearKey == null ? (existing == null ? null : existing.apiKeyUpdatedAt()) : DateUtil011.now());

        if (existing == null) {
            repository.insert(config);
        } else {
            repository.update(config);
        }

        return toView(config, clearKey);
    }

    /**
     * Rotate the api key of an object (returned in clear exactly once).
     *
     * @param objectName object name
     * @return configuration view with the clear api key
     */
    public Map<String, Object> rotateApiKey(String objectName) {
        JulyMetadataOpenApi existing = repository.findByObjectName(objectName);

        if (existing == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        String clearKey = randomKey();
        JulyMetadataOpenApi rotated = new JulyMetadataOpenApi(existing.id(), objectName, true, "apiKey",
                existing.allowedOps(), clearKey, DateUtil011.now());
        repository.update(rotated);

        return toView(rotated, clearKey);
    }

    /**
     * Build the masked view.
     *
     * @param config   configuration
     * @param clearKey clear key to expose once, nullable
     * @return view
     */
    private Map<String, Object> toView(JulyMetadataOpenApi config, String clearKey) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("objectName", config.objectName());
        view.put("enabled", config.enabled());
        view.put("authMode", config.authMode());
        view.put("allowedOps", splitOps(config.allowedOps()));
        view.put("apiKeyConfigured", config.apiKey() != null && !config.apiKey().isBlank());
        view.put("apiKeyHint", config.apiKey() == null || config.apiKey().length() < 4
                ? null
                : "****" + config.apiKey().substring(config.apiKey().length() - 4));
        view.put("apiKeyUpdatedAt", config.apiKeyUpdatedAt());
        view.put("openApiBasePath", BASE_PATH + config.objectName());

        if (clearKey != null) {
            view.put("apiKey", clearKey);
        }

        return view;
    }

    /**
     * Generate a 32-char hex api key.
     *
     * @return api key
     */
    private String randomKey() {
        char[] out = new char[32];

        for (int i = 0; i < out.length; i++) {
            out[i] = HEX[RANDOM.nextInt(HEX.length)];
        }

        return new String(out);
    }

    /**
     * Split a comma list of operations.
     *
     * @param ops comma list
     * @return operations, default query
     */
    private List<String> splitOps(String ops) {
        if (ops == null || ops.isBlank()) {
            return List.of("query");
        }

        return List.of(ops.split(","));
    }

    /**
     * Join the allowed operations from a raw payload value.
     *
     * @param value raw list or string
     * @return comma list, default query
     */
    @SuppressWarnings("unchecked")
    private String joinOps(Object value) {
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            return list.isEmpty() ? "query" : list.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).get();
        }

        return defaultIfBlank(value == null ? null : String.valueOf(value), "query");
    }

    /**
     * Read a string value.
     *
     * @param value raw value
     * @return string or null
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Fall back to a default when blank.
     *
     * @param value    raw value
     * @param fallback default
     * @return value or default
     */
    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

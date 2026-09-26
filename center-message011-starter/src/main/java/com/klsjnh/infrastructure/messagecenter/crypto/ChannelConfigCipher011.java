package com.klsjnh.infrastructure.messagecenter.crypto;

/*                ChannelConfigCipher011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  field-level secret cipher for channel config json
 *      2026.09.26  expand sensitive keys (corpSecret / appSecret / …)
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.crypto.SecretCipherPort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Field-level encrypt / decrypt / mask helpers for message-channel
 * {@code config} JSON. Only known sensitive string keys are rewritten; other
 * keys and non-string values pass through. Legacy plaintext (no
 * {@code enc:v1:} prefix) remains readable via {@link SecretCipherPort}.
 */

public final class ChannelConfigCipher011 {

    /**
     * Mask shown to API clients for sensitive values.
     */
    public static final String MASK = "******";

    /**
     * Sensitive key names, compared after lowercasing and stripping
     * underscores / hyphens (so {@code api_key} / {@code apiKey} /
     * {@code corpSecret} all match). Exact names plus common vendor compounds.
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "secret", "token", "password", "apikey", "accesskey", "secretkey",
            "corpsecret", "appsecret", "clientsecret", "privatekey");

    /**
     * Suffixes that mark compound keys as secrets ({@code wecomSecret},
     * {@code refreshToken}, …) after the same normalization.
     */
    private static final String[] SENSITIVE_SUFFIXES = {
            "secret", "token", "password", "apikey", "accesskey", "secretkey", "privatekey"
    };

    /**
     * Shared mapper for config JSON (thread-safe for read/writeValue).
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Utility — no instances.
     */
    private ChannelConfigCipher011() {
    }

    /**
     * Whether a config map key is treated as a secret.
     *
     * @param key raw JSON key, nullable
     * @return true when the key is in the sensitive set or ends with a
     *         sensitive suffix
     */
    public static boolean isSensitiveKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");

        if (SENSITIVE_KEYS.contains(normalized)) {
            return true;
        }

        for (String suffix : SENSITIVE_SUFFIXES) {
            if (normalized.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Encrypt sensitive string fields in a config JSON for durable storage.
     * Blank / unparseable input is returned unchanged.
     *
     * @param configJson   config JSON, nullable
     * @param secretCipher cipher port
     * @return JSON with sensitive values encrypted, or the original blank/null
     */
    public static String encryptFields(String configJson, SecretCipherPort secretCipher) {
        return transform(configJson, secretCipher, true);
    }

    /**
     * Decrypt sensitive string fields when hydrating an aggregate. Legacy
     * plaintext values (no cipher prefix) pass through.
     *
     * @param configJson   config JSON from the database, nullable
     * @param secretCipher cipher port
     * @return JSON with sensitive values decrypted, or the original blank/null
     */
    public static String decryptFields(String configJson, SecretCipherPort secretCipher) {
        return transform(configJson, secretCipher, false);
    }

    /**
     * Mask sensitive string fields for API responses (never echo plaintext).
     *
     * @param configJson config JSON with plaintext secrets, nullable
     * @return JSON with sensitive values replaced by {@link #MASK}
     */
    public static String maskFields(String configJson) {
        if (StringUtil011.isBlank(configJson)) {
            return configJson;
        }

        Map<String, Object> map = parseMap(configJson);

        if (map == null) {
            return configJson;
        }

        boolean changed = false;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (isSensitiveKey(entry.getKey()) && entry.getValue() instanceof String value
                    && !StringUtil011.isBlank(value)) {
                entry.setValue(MASK);
                changed = true;
            }
        }

        return changed ? writeMap(map) : configJson;
    }

    /**
     * Merge an incoming update config onto the existing plaintext config so
     * that masked ({@link #MASK}) or blank sensitive values keep the stored
     * secret instead of overwriting it.
     *
     * @param existingPlain existing decrypted config JSON, nullable
     * @param incoming      update config from the client, nullable
     * @return merged config JSON for persistence (plaintext in memory)
     */
    public static String mergeKeepingSecrets(String existingPlain, String incoming) {
        if (StringUtil011.isBlank(incoming)) {
            return incoming;
        }

        Map<String, Object> next = parseMap(incoming);

        if (next == null) {
            return incoming;
        }

        Map<String, Object> prev = parseMap(existingPlain);

        if (prev == null || prev.isEmpty()) {
            return incoming;
        }

        boolean changed = false;

        for (Map.Entry<String, Object> entry : next.entrySet()) {
            if (!isSensitiveKey(entry.getKey())) {
                continue;
            }

            Object value = entry.getValue();

            if (!(value instanceof String text)) {
                continue;
            }

            if (StringUtil011.isBlank(text) || MASK.equals(text.trim())) {
                Object kept = prev.get(entry.getKey());
                if (kept != null) {
                    entry.setValue(kept);
                    changed = true;
                }
            }
        }

        return changed ? writeMap(next) : incoming;
    }

    /**
     * Encrypt or decrypt sensitive string fields.
     *
     * @param configJson   config JSON
     * @param secretCipher cipher port
     * @param encrypt      true to encrypt, false to decrypt
     * @return transformed JSON
     */
    private static String transform(String configJson, SecretCipherPort secretCipher, boolean encrypt) {
        if (StringUtil011.isBlank(configJson) || secretCipher == null) {
            return configJson;
        }

        Map<String, Object> map = parseMap(configJson);

        if (map == null) {
            return configJson;
        }

        boolean changed = false;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!isSensitiveKey(entry.getKey()) || !(entry.getValue() instanceof String value)) {
                continue;
            }

            if (StringUtil011.isBlank(value)) {
                continue;
            }

            String next = encrypt ? secretCipher.encrypt(value) : secretCipher.decrypt(value);

            if (!value.equals(next)) {
                entry.setValue(next);
                changed = true;
            }
        }

        return changed ? writeMap(map) : configJson;
    }

    /**
     * Parse a flat JSON object into a mutable map.
     *
     * @param configJson JSON text
     * @return map, or null when blank / unparseable
     */
    private static Map<String, Object> parseMap(String configJson) {
        if (StringUtil011.isBlank(configJson)) {
            return null;
        }

        try {
            Map<String, Object> raw = MAPPER.readValue(configJson, new TypeReference<Map<String, Object>>() {
            });

            return raw == null ? null : new LinkedHashMap<>(raw);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    /**
     * Serialize a map back to JSON.
     *
     * @param map map
     * @return JSON text
     */
    private static String writeMap(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("channel config json serialization failed", ex);
        }
    }
}

package com.klsjnh.domain.system011.config;

/*                JulyConfig class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyConfig aggregate root (system management context): one runtime
 * key-value parameter — code is the program-facing key (unique, immutable),
 * data is a String value parsed by the consumer.
 * <p>
 * Boundary: framework's own config lives in application.yml (KrtConfig011);
 * the same key must never exist in both homes.
 * </p>
 */

public class JulyConfig {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Config key, unique, immutable after create.
     */
    private final String code;

    /**
     * Config value (String; consumer parses booleans / numbers / JSON).
     */
    private String data;

    /**
     * Config status: '1' enabled / '0' disabled (disabled is invisible to
     * readers).
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id    primary key
     * @param code  config key, unique
     * @param data  config value
     * @param status config status
     * @param audit audit info
     */
    public JulyConfig(EntityId id, String code, String data, String status, AuditInfo audit) {
        this.id = id;
        this.code = code;
        this.data = data;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new config entry.
     *
     * @param id    primary key
     * @param code  config key, unique, max 60
     * @param data  config value, max 300
     * @param audit audit info
     * @return new aggregate
     */
    public static JulyConfig create(EntityId id, String code, String data, AuditInfo audit) {
        validate(code, data);

        return new JulyConfig(id, code, data, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the value (code is immutable after create).
     *
     * @param data config value
     */
    public void updateData(String data) {
        validate(this.code, data);
        this.data = data;
    }

    /**
     * Validate the create / update basics.
     *
     * @param code config key
     * @param data config value
     */
    private static void validate(String code, String data) {
        if (StringUtil011.isMissing(code, 60)) {
            throw new IllegalArgumentException("config code is required (max 60)");
        }

        if (StringUtil011.isMissing(data, 300)) {
            throw new IllegalArgumentException("config data is required (max 300)");
        }
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the config key.
     *
     * @return config key
     */
    public String code() {
        return code;
    }

    /**
     * Get the config value.
     *
     * @return config value
     */
    public String data() {
        return data;
    }

    /**
     * Get the config status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}

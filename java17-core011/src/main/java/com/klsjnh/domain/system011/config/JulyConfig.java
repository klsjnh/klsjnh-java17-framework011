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
 * Boundary: framework's own config lives in application.yml (the krt.* config binding classes);
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
     * Remark, optional.
     */
    private String remark;

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
     * @param remark remark, optional, max 300
     * @param audit audit info
     */
    public JulyConfig(EntityId id, String code, String data, String status, String remark, AuditInfo audit) {
        this.id = id;
        this.code = code;
        this.data = data;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new config entry.
     *
     * @param id     primary key
     * @param code   config key, unique, max 60
     * @param data   config value, max 300
     * @param status config status, null defaults to enabled
     * @param remark remark, optional, max 300
     * @param audit  audit info
     * @return new aggregate
     */
    public static JulyConfig create(EntityId id, String code, String data, String status, String remark,
            AuditInfo audit) {
        validate(code, data, remark);

        return new JulyConfig(id, code, data, status, remark, audit);
    }

    /**
     * Update the value / status (code is immutable after create).
     *
     * @param data   config value
     * @param status config status, null keeps the stored one
     * @param remark remark, optional, max 300
     */
    public void updateData(String data, String status, String remark) {
        validate(this.code, data, remark);
        this.data = data;
        this.remark = remark;

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the create / update basics.
     *
     * @param code config key
     * @param data config value
     * @param remark remark, optional
     */
    private static void validate(String code, String data, String remark) {
        StringUtil011.requirePresent(code, "config code", 60);

        StringUtil011.requirePresent(data, "config data", 300);

        StringUtil011.requireMax(remark, "remark", 300);
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
     * Get the remark.
     *
     * @return remark, nullable
     */
    public String remark() {
        return remark;
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

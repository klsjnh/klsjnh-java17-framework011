package com.klsjnh.demo11.domain;

/*                Demo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * Demo011 aggregate root: the reference business aggregate showing third
 * parties how to build on the framework (one code/name table, the full DDD
 * vertical slice inside this package).
 */

public class Demo011 {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Demo code, unique, immutable after create.
     */
    private final String code;

    /**
     * Demo name.
     */
    private String name;

    /**
     * Row status: '1' enabled / '0' disabled.
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
     * @param code  demo code, unique
     * @param name  demo name
     * @param status row status
     * @param audit audit info
     */
    public Demo011(EntityId id, String code, String name, String status, AuditInfo audit) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new demo row.
     *
     * @param id    primary key
     * @param code  demo code, unique, max 30
     * @param name  demo name, max 60
     * @param audit audit info
     * @return new aggregate
     */
    public static Demo011 create(EntityId id, String code, String name, AuditInfo audit) {
        validate(code, name);

        return new Demo011(id, code, name, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the name (code is immutable after create).
     *
     * @param name demo name
     */
    public void updateName(String name) {
        StringUtil011.requirePresent(name, "demo name", 60);

        this.name = name;
    }

    /**
     * Validate the create basics.
     *
     * @param code demo code
     * @param name demo name
     */
    private static void validate(String code, String name) {
        StringUtil011.requirePresent(code, "demo code", 30);
        StringUtil011.requirePresent(name, "demo name", 60);
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
     * Get the demo code.
     *
     * @return demo code
     */
    public String code() {
        return code;
    }

    /**
     * Get the demo name.
     *
     * @return demo name
     */
    public String name() {
        return name;
    }

    /**
     * Get the row status.
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

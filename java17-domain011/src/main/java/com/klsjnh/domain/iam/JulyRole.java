package com.klsjnh.domain.iam;

/*                JulyRole class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role class
 *      2026.09.15  add changeStatus (status editable via update)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyRole aggregate root (system management context): a role collecting
 * permission codes from the menu tree. Built-in roles are protected against
 * delete and disable.
 */

public class JulyRole {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Role code, unique, immutable after create.
     */
    private final String roleCode;

    /**
     * Role name.
     */
    private String roleName;

    /**
     * Built-in flag: '1' built-in (protected) / '0' custom.
     */
    private String isBuiltin;

    /**
     * Remark.
     */
    private String remark;

    /**
     * Role status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id        primary key
     * @param roleCode  role code, unique
     * @param roleName  role name
     * @param isBuiltin built-in flag
     * @param remark    remark
     * @param status    role status
     * @param audit     audit info
     */
    public JulyRole(EntityId id, String roleCode, String roleName, String isBuiltin, String remark, String status,
            AuditInfo audit) {
        this.id = id;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.isBuiltin = isBuiltin;
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a custom role: defaults to enabled, not built-in.
     *
     * @param id       primary key
     * @param roleCode role code, unique, max 30
     * @param roleName role name, max 60
     * @param remark   remark
     * @param audit    audit info
     * @return new aggregate in enabled state
     */
    public static JulyRole create(EntityId id, String roleCode, String roleName, String remark, AuditInfo audit) {
        validate(roleCode, roleName);
        return new JulyRole(id, roleCode, roleName, "0", remark, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable basics (code is immutable after create).
     *
     * @param roleName role name
     * @param remark   remark
     */
    public void updateBasics(String roleName, String remark) {
        if (roleName == null || roleName.isBlank() || roleName.length() > 60) {
            throw new IllegalArgumentException("role name is required (max 60)");
        }

        this.roleName = roleName;
        this.remark = remark;
    }

    /**
     * Switch the role to enabled.
     */
    public void enable() {
        this.status = Status011.ENABLED.getCode();
    }

    /**
     * Switch the role to disabled.
     */
    public void disable() {
        this.status = Status011.DISABLED.getCode();
    }

    /**
     * Apply an explicit status value (used by the update path). Resolves the
     * raw column value and delegates to {@link #enable()} / {@link #disable()}.
     * Rejects unknown values instead of silently keeping the current status.
     *
     * @param status raw status column value ("1" / "0")
     */
    public void changeStatus(String status) {
        Status011 next = Status011.of(status);

        if (next == null) {
            throw new IllegalArgumentException("invalid role status: " + status);
        }

        if (next == Status011.ENABLED) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * Validate the create basics.
     *
     * @param roleCode role code
     * @param roleName role name
     */
    private static void validate(String roleCode, String roleName) {
        StringUtil011.requirePresent(roleCode, "role code", 30);

        StringUtil011.requirePresent(roleName, "role name", 60);
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
     * Get the role code.
     *
     * @return role code
     */
    public String roleCode() {
        return roleCode;
    }

    /**
     * Get the role name.
     *
     * @return role name
     */
    public String roleName() {
        return roleName;
    }

    /**
     * Get the built-in flag.
     *
     * @return built-in flag
     */
    public String isBuiltin() {
        return isBuiltin;
    }

    /**
     * Get the remark.
     *
     * @return remark
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the role status.
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

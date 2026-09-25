package com.klsjnh.domain.iam.perm;

/*                JulyPermAction class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission catalog action aggregate
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * Permission catalog action: object + action → full permission_code.
 */

public class JulyPermAction {

    /**
     * Default sort order.
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Parent object code.
     */
    private final String objectCode;

    /**
     * Action code (start / select / …).
     */
    private final String actionCode;

    /**
     * Display name.
     */
    private String actionName;

    /**
     * Full permission code (module:object:action).
     */
    private final String permissionCode;

    /**
     * Manual sort order.
     */
    private Integer sortOrder;

    /**
     * Row status.
     */
    private String status;

    /**
     * Remark.
     */
    private String remark;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also rehydration).
     *
     * @param id             primary key
     * @param objectCode     object code
     * @param actionCode     action code
     * @param actionName     display name
     * @param permissionCode full permission code
     * @param sortOrder      sort
     * @param status         status
     * @param remark         remark
     * @param audit          audit
     */
    public JulyPermAction(EntityId id, String objectCode, String actionCode, String actionName, String permissionCode,
            Integer sortOrder, String status, String remark, AuditInfo audit) {
        this.id = id;
        this.objectCode = objectCode;
        this.actionCode = actionCode;
        this.actionName = actionName;
        this.permissionCode = permissionCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new enabled action.
     *
     * @param id             primary key
     * @param objectCode     object code
     * @param actionCode     action code
     * @param actionName     display name
     * @param permissionCode full permission code
     * @param sortOrder      sort, nullable
     * @param remark         remark
     * @param audit          audit
     * @return aggregate
     */
    public static JulyPermAction create(EntityId id, String objectCode, String actionCode, String actionName,
            String permissionCode, Integer sortOrder, String remark, AuditInfo audit) {
        if (StringUtil011.isBlank(objectCode) || StringUtil011.isBlank(actionCode)
                || StringUtil011.isBlank(permissionCode)) {
            throw new IllegalArgumentException("objectCode, actionCode and permissionCode are required");
        }

        return new JulyPermAction(id, objectCode.trim(), actionCode.trim(), actionName, permissionCode.trim(),
                sortOrder, Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * @return primary key
     */
    public EntityId id() {
        return id;
    }

    /**
     * @return object code
     */
    public String objectCode() {
        return objectCode;
    }

    /**
     * @return action code
     */
    public String actionCode() {
        return actionCode;
    }

    /**
     * @return display name
     */
    public String actionName() {
        return actionName;
    }

    /**
     * @return full permission code
     */
    public String permissionCode() {
        return permissionCode;
    }

    /**
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * @return status
     */
    public String status() {
        return status;
    }

    /**
     * @return remark
     */
    public String remark() {
        return remark;
    }

    /**
     * @return audit
     */
    public AuditInfo audit() {
        return audit;
    }
}

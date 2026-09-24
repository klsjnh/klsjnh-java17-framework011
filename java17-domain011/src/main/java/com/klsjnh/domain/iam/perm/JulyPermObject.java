package com.klsjnh.domain.iam.perm;

/*                JulyPermObject class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission catalog object aggregate
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * Permission catalog object (e.g. julyScheduler / julyConfig): one securable
 * resource type under a module.
 */

public class JulyPermObject {

    /**
     * Default sort order.
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Object code, unique, immutable.
     */
    private final String objectCode;

    /**
     * Display name.
     */
    private String objectName;

    /**
     * Module code (system011 / iam / …).
     */
    private String moduleCode;

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
     * @param id         primary key
     * @param objectCode object code
     * @param objectName display name
     * @param moduleCode module
     * @param sortOrder  sort
     * @param status     status
     * @param remark     remark
     * @param audit      audit
     */
    public JulyPermObject(EntityId id, String objectCode, String objectName, String moduleCode, Integer sortOrder,
            String status, String remark, AuditInfo audit) {
        this.id = id;
        this.objectCode = objectCode;
        this.objectName = objectName;
        this.moduleCode = moduleCode == null ? "" : moduleCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new enabled object.
     *
     * @param id         primary key
     * @param objectCode object code
     * @param objectName display name
     * @param moduleCode module
     * @param sortOrder  sort, nullable
     * @param remark     remark
     * @param audit      audit
     * @return aggregate
     */
    public static JulyPermObject create(EntityId id, String objectCode, String objectName, String moduleCode,
            Integer sortOrder, String remark, AuditInfo audit) {
        if (StringUtil011.isBlank(objectCode)) {
            throw new IllegalArgumentException("objectCode is required");
        }

        return new JulyPermObject(id, objectCode.trim(), objectName, moduleCode, sortOrder,
                Status011.ENABLED.getCode(), remark, audit);
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
     * @return display name
     */
    public String objectName() {
        return objectName;
    }

    /**
     * @return module code
     */
    public String moduleCode() {
        return moduleCode;
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

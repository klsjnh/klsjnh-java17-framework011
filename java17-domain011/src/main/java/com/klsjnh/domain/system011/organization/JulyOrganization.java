package com.klsjnh.domain.system011.organization;

/*                JulyOrganization class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.ArrayList;
import java.util.List;

/**
 * JulyOrganization aggregate root (system management context): one node of
 * the organization tree — the mount point of users (july_user.pk_org) and the
 * source of the member-count badge.
 */

public class JulyOrganization {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Organization code, unique, immutable after create.
     */
    private final String orgCode;

    /**
     * Organization name.
     */
    private String orgName;

    /**
     * Leader user id (pk_user → july_user.id), nullable.
     */
    private String pkUser;

    /**
     * Redundant tree level (root = 1), maintained by the use case.
     */
    private Integer orgLevel;

    /**
     * Parent organization id, blank for root.
     */
    private String parentId;

    /**
     * Sort order within siblings.
     */
    private Integer sortOrder;

    /**
     * Organization status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Nested child nodes, not persistent state — filled by tree assembly.
     */
    private final List<JulyOrganization> children = new ArrayList<>();

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id        primary key
     * @param orgCode   organization code, unique
     * @param orgName   organization name
     * @param pkUser    leader user id, nullable
     * @param orgLevel  tree level
     * @param parentId  parent organization id
     * @param sortOrder sort order
     * @param status    organization status
     * @param audit     audit info
     */
    public JulyOrganization(EntityId id, String orgCode, String orgName, String pkUser, Integer orgLevel,
            String parentId, Integer sortOrder, String status, AuditInfo audit) {
        this.id = id;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.pkUser = pkUser;
        this.orgLevel = orgLevel;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new organization node.
     *
     * @param id        primary key
     * @param orgCode   organization code, unique, max 30
     * @param orgName   organization name, max 60
     * @param pkUser    leader user id, nullable
     * @param orgLevel  tree level (root = 1)
     * @param parentId  parent organization id, blank for root
     * @param sortOrder sort order
     * @param audit     audit info
     * @return new aggregate
     */
    public static JulyOrganization create(EntityId id, String orgCode, String orgName, String pkUser, Integer orgLevel,
            String parentId, Integer sortOrder, AuditInfo audit) {
        validateBasics(orgCode, orgName);

        return new JulyOrganization(id, orgCode, orgName, pkUser, orgLevel, parentId, sortOrder,
                Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable basics (code immutable; parent move allowed — the
     * use case re-levels the subtree).
     *
     * @param orgName   organization name
     * @param pkUser    leader user id, nullable
     * @param parentId  parent organization id
     * @param sortOrder sort order
     */
    public void updateBasics(String orgName, String pkUser, String parentId, Integer sortOrder) {
        validateBasics(this.orgCode, orgName);
        this.orgName = orgName;
        this.pkUser = pkUser;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    /**
     * Change the redundant tree level (parent move re-levels the subtree).
     *
     * @param orgLevel new level
     */
    public void changeLevel(Integer orgLevel) {
        this.orgLevel = orgLevel;
    }

    /**
     * Attach a child node during tree assembly.
     *
     * @param child child node
     */
    public void addChild(JulyOrganization child) {
        children.add(child);
    }

    /**
     * Validate the create / update basics.
     *
     * @param orgCode organization code
     * @param orgName organization name
     */
    private static void validateBasics(String orgCode, String orgName) {
        StringUtil011.requirePresent(orgCode, "org code", 30);

        StringUtil011.requirePresent(orgName, "org name", 60);
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
     * Get the organization code.
     *
     * @return organization code
     */
    public String orgCode() {
        return orgCode;
    }

    /**
     * Get the organization name.
     *
     * @return organization name
     */
    public String orgName() {
        return orgName;
    }

    /**
     * Get the leader user id.
     *
     * @return leader user id or null
     */
    public String pkUser() {
        return pkUser;
    }

    /**
     * Get the tree level.
     *
     * @return tree level
     */
    public Integer orgLevel() {
        return orgLevel;
    }

    /**
     * Get the parent organization id.
     *
     * @return parent id, blank for root
     */
    public String parentId() {
        return parentId;
    }

    /**
     * Get the sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the organization status.
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

    /**
     * Get the nested children.
     *
     * @return children
     */
    public List<JulyOrganization> getChildren() {
        return children;
    }
}

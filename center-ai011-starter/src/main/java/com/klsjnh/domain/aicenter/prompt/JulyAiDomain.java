package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomain class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain aggregate (master tree)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.ArrayList;
import java.util.List;

/**
 * AI prompt business domain aggregate root (master tree): one node of the
 * {@code july_ai_domain} tree. The prompts themselves live in the
 * {@link JulyAiDomainPrompt} child aggregate.
 */

public class JulyAiDomain {

    /** Default sort order. */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /** Primary key. */
    private final EntityId id;

    /** Domain code, globally unique and immutable. */
    private final String domainCode;

    /** Domain name. */
    private String domainName;

    /** Parent domain id, blank for root. */
    private String parentId;

    /** Sort order. */
    private Integer sortOrder;

    /** Row status. */
    private String status;

    /** Remark. */
    private String remark;

    /** Audit info. */
    private AuditInfo audit;

    /** Nested child nodes, not persistent state — filled by tree assembly. */
    private final List<JulyAiDomain> children = new ArrayList<>();

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id         primary key
     * @param domainCode domain code
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order
     * @param status     row status
     * @param remark     remark
     * @param audit      audit info
     */
    public JulyAiDomain(EntityId id, String domainCode, String domainName, String parentId, Integer sortOrder,
            String status, String remark, AuditInfo audit) {
        validate(domainCode, domainName);

        this.id = id;
        this.domainCode = domainCode;
        this.domainName = domainName;
        this.parentId = parentId == null ? "" : parentId;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new domain node.
     *
     * @param id         primary key
     * @param domainCode domain code
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order
     * @param remark     remark
     * @param audit      audit info
     * @return new aggregate
     */
    public static JulyAiDomain create(EntityId id, String domainCode, String domainName, String parentId,
            Integer sortOrder, String remark, AuditInfo audit) {
        return new JulyAiDomain(id, domainCode, domainName, parentId, sortOrder, Status011.ENABLED.getCode(), remark,
                audit);
    }

    /**
     * Update the mutable fields (domainCode is immutable).
     *
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order, null keeps the stored one
     * @param remark     remark
     * @param status     row status, null keeps the stored one
     */
    public void update(String domainName, String parentId, Integer sortOrder, String remark, String status) {
        validate(this.domainCode, domainName);

        this.domainName = domainName;
        this.parentId = parentId == null ? "" : parentId;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Attach a child node during tree assembly.
     *
     * @param child child node
     */
    public void addChild(JulyAiDomain child) {
        children.add(child);
    }

    /**
     * Validate the required fields.
     *
     * @param domainCode domain code
     * @param domainName domain name
     */
    private static void validate(String domainCode, String domainName) {
        StringUtil011.requirePresent(domainCode, "domain code", 60);
        StringUtil011.requirePresent(domainName, "domain name", 100);
    }

    /** @return primary key */
    public EntityId id() {
        return id;
    }

    /** @return domain code */
    public String domainCode() {
        return domainCode;
    }

    /** @return domain name */
    public String domainName() {
        return domainName;
    }

    /** @return parent domain id, blank for root */
    public String parentId() {
        return parentId;
    }

    /** @return sort order */
    public Integer sortOrder() {
        return sortOrder;
    }

    /** @return row status */
    public String status() {
        return status;
    }

    /** @return remark */
    public String remark() {
        return remark;
    }

    /** @return audit info */
    public AuditInfo audit() {
        return audit;
    }

    /** @return nested children */
    public List<JulyAiDomain> children() {
        return children;
    }
}

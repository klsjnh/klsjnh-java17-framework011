package com.klsjnh.domain.aicenter.modelprovider;

/*                AiModelProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * AiModelProvider aggregate root (AI model access context): one vendor's access
 * point — provider code (unique, immutable), display name, OpenAI-compatible
 * base url and a comma separated model list.
 */

public class AiModelProvider {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Provider code, unique, immutable.
     */
    private final String providerCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Provider display name.
     */
    private String providerName;

    /**
     * OpenAI-compatible base url.
     */
    private String baseUrl;

    /**
     * Comma separated model list, optional.
     */
    private String models;

    /**
     * Row status: '1' enabled / '0' disabled.
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
     * @param id           primary key
     * @param providerCode provider code, unique
     * @param sortOrder    manual sort order, null falls back to the default
     * @param providerName provider display name
     * @param baseUrl      base url
     * @param models       comma separated model list, optional
     * @param status       row status
     * @param remark       remark, optional
     * @param audit        audit info
     */
    public AiModelProvider(EntityId id, String providerCode, Integer sortOrder, String providerName, String baseUrl,
            String models, String status, String remark, AuditInfo audit) {
        this.id = id;
        this.providerCode = providerCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.providerName = providerName;
        this.baseUrl = baseUrl;
        this.models = models;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new provider.
     *
     * @param id           primary key
     * @param providerCode provider code, unique, max 60
     * @param sortOrder    manual sort order, null falls back to the default
     * @param providerName provider display name, max 100
     * @param baseUrl      base url, max 300
     * @param models       comma separated model list, optional, max 500
     * @param remark       remark, optional, max 300
     * @param audit        audit info
     * @return new aggregate
     */
    public static AiModelProvider create(EntityId id, String providerCode, Integer sortOrder, String providerName,
            String baseUrl, String models, String remark, AuditInfo audit) {
        validate(providerCode, providerName, baseUrl, models, remark);

        return new AiModelProvider(id, providerCode, sortOrder, providerName, baseUrl, models,
                Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * Update the mutable fields (providerCode is immutable).
     *
     * @param providerName provider display name
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param baseUrl      base url
     * @param models       comma separated model list, optional
     * @param status       row status, null keeps the stored one
     * @param remark       remark, optional
     */
    public void update(String providerName, Integer sortOrder, String baseUrl, String models, String status,
            String remark) {
        validate(this.providerCode, providerName, baseUrl, models, remark);
        this.providerName = providerName;
        this.baseUrl = baseUrl;
        this.models = models;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the shared create / update basics.
     *
     * @param providerCode provider code
     * @param providerName provider display name
     * @param baseUrl      base url
     * @param models       comma separated model list
     * @param remark       remark
     */
    private static void validate(String providerCode, String providerName, String baseUrl, String models,
            String remark) {
        StringUtil011.requirePresent(providerCode, "provider code", 60);

        StringUtil011.requirePresent(providerName, "provider name", 100);

        StringUtil011.requirePresent(baseUrl, "base url", 300);

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("base url must start with http:// or https://");
        }

        StringUtil011.requireMax(models, "models", 500);

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
     * Get the provider code.
     *
     * @return provider code
     */
    public String providerCode() {
        return providerCode;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order, smaller comes first
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the provider display name.
     *
     * @return provider name
     */
    public String providerName() {
        return providerName;
    }

    /**
     * Get the base url.
     *
     * @return base url
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Get the comma separated model list.
     *
     * @return models or null
     */
    public String models() {
        return models;
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
     * Get the remark.
     *
     * @return remark or null
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

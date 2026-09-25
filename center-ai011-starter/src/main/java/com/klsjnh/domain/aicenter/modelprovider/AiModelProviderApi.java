package com.klsjnh.domain.aicenter.modelprovider;

/*                AiModelProviderApi class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * AiModelProviderApi entity: one API key of a provider (multi-account /
 * quota). The key is a secret — it never crosses the web boundary.
 */

public class AiModelProviderApi {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Master link (provider id).
     */
    private final String pkMt;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Api key code, unique within the provider, immutable.
     */
    private final String apiCode;

    /**
     * Api key display name.
     */
    private String apiName;

    /**
     * API key secret; never echoed back by the web layer.
     */
    private String apiKey;

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
     * @param id        primary key
     * @param pkMt      master link (provider id)
     * @param sortOrder manual sort order, null falls back to the default
     * @param apiCode   api key code, unique within the provider
     * @param apiName   api key display name
     * @param apiKey    api key secret
     * @param status    row status
     * @param remark    remark, optional
     * @param audit     audit info
     */
    public AiModelProviderApi(EntityId id, String pkMt, Integer sortOrder, String apiCode, String apiName,
            String apiKey, String status, String remark, AuditInfo audit) {
        this.id = id;
        this.pkMt = pkMt;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.apiKey = apiKey;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new api key.
     *
     * @param id        primary key
     * @param pkMt      master link (provider id)
     * @param sortOrder manual sort order, null falls back to the default
     * @param apiCode   api key code, unique within the provider, max 60
     * @param apiName   api key display name, max 100
     * @param apiKey    api key secret, max 300
     * @param remark    remark, optional, max 300
     * @param audit     audit info
     * @return new entity
     */
    public static AiModelProviderApi create(EntityId id, String pkMt, Integer sortOrder, String apiCode, String apiName,
            String apiKey, String remark, AuditInfo audit) {
        validate(pkMt, apiCode, apiName, remark);

        StringUtil011.requirePresent(apiKey, "api key", 300);

        return new AiModelProviderApi(id, pkMt, sortOrder, apiCode, apiName, apiKey, Status011.ENABLED.getCode(),
                remark, audit);
    }

    /**
     * Update the mutable fields (apiCode is immutable). A blank apiKey keeps the
     * stored one (the web layer never echoes it back).
     *
     * @param apiName   api key display name
     * @param sortOrder manual sort order, null keeps the stored one
     * @param apiKey    api key secret, blank keeps the stored one
     * @param status    row status, null keeps the stored one
     * @param remark    remark, optional
     */
    public void update(String apiName, Integer sortOrder, String apiKey, String status, String remark) {
        validate(this.pkMt, this.apiCode, apiName, remark);

        StringUtil011.requireMax(apiKey, "api key", 300);

        this.apiName = apiName;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(apiKey)) {
            this.apiKey = apiKey;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the shared create / update basics.
     *
     * @param pkMt    master link
     * @param apiCode api key code
     * @param apiName api key display name
     * @param remark  remark
     */
    private static void validate(String pkMt, String apiCode, String apiName, String remark) {
        StringUtil011.requirePresent(pkMt, "provider link", 33);

        StringUtil011.requirePresent(apiCode, "api code", 60);

        StringUtil011.requirePresent(apiName, "api name", 100);

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
     * Get the master link.
     *
     * @return provider id
     */
    public String pkMt() {
        return pkMt;
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
     * Get the api key code.
     *
     * @return api code
     */
    public String apiCode() {
        return apiCode;
    }

    /**
     * Get the api key display name.
     *
     * @return api name
     */
    public String apiName() {
        return apiName;
    }

    /**
     * Get the api key secret.
     *
     * @return api key
     */
    public String apiKey() {
        return apiKey;
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

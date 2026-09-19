package com.klsjnh.domain.messagecenter.channel;

/*                JulyMessageChannel class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyMessageChannel aggregate root (message center context): one channel
 * configuration row — channel code (unique, immutable), a display name, the
 * provider type binding the SPI channel and an opaque JSON config.
 */

public class JulyMessageChannel {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Channel code, unique and immutable.
     */
    private final String channelCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Channel display name.
     */
    private String channelName;

    /**
     * Provider type: the SPI channelCode this config binds to.
     */
    private String providerType;

    /**
     * Opaque JSON config (url / secret ...), never echoed back in raw form.
     */
    private String config;

    /**
     * Remark, optional.
     */
    private String remark;

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
     * @param id           primary key
     * @param channelCode  channel code, unique
     * @param sortOrder    manual sort order, null falls back to the default
     * @param channelName  channel display name
     * @param providerType provider type (SPI channelCode)
     * @param config       JSON config, optional
     * @param status       row status
     * @param remark       remark, optional
     * @param audit        audit info
     */
    public JulyMessageChannel(EntityId id, String channelCode, Integer sortOrder, String channelName,
            String providerType, String config, String status, String remark, AuditInfo audit) {
        validate(channelCode, channelName, providerType, config, remark);

        this.id = id;
        this.channelCode = channelCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.channelName = channelName;
        this.providerType = providerType;
        this.config = config;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new channel.
     *
     * @param id           primary key
     * @param channelCode  channel code, unique, max 60
     * @param sortOrder    manual sort order, null falls back to the default
     * @param channelName  channel display name, max 100
     * @param providerType provider type, max 60
     * @param config       JSON config, optional
     * @param remark       remark, optional, max 300
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulyMessageChannel create(EntityId id, String channelCode, Integer sortOrder, String channelName,
            String providerType, String config, String remark, AuditInfo audit) {
        return new JulyMessageChannel(id, channelCode, sortOrder, channelName, providerType, config,
                Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * Update the mutable fields (channelCode is immutable).
     *
     * @param channelName  channel display name
     * @param providerType provider type
     * @param config       JSON config
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param status       row status, null keeps the stored one
     * @param remark       remark
     */
    public void update(String channelName, String providerType, String config, Integer sortOrder, String status,
            String remark) {
        validate(this.channelCode, channelName, providerType, config, remark);

        this.channelName = channelName;
        this.providerType = providerType;
        this.config = config;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the channel fields.
     *
     * @param channelCode  channel code
     * @param channelName  channel display name
     * @param providerType provider type
     * @param config       JSON config
     * @param remark       remark
     */
    private static void validate(String channelCode, String channelName, String providerType, String config,
            String remark) {
        StringUtil011.requirePresent(channelCode, "channel code", 60);
        StringUtil011.requirePresent(channelName, "channel name", 100);
        StringUtil011.requirePresent(providerType, "provider type", 60);
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
     * Get the channel code.
     *
     * @return channel code
     */
    public String channelCode() {
        return channelCode;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the display name.
     *
     * @return channel name
     */
    public String channelName() {
        return channelName;
    }

    /**
     * Get the provider type.
     *
     * @return provider type
     */
    public String providerType() {
        return providerType;
    }

    /**
     * Get the JSON config.
     *
     * @return config or null
     */
    public String config() {
        return config;
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

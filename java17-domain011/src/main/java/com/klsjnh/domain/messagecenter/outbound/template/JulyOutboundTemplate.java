package com.klsjnh.domain.messagecenter.outbound.template;

/*                JulyOutboundTemplate class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyOutboundTemplate aggregate root (message center context): one message
 * template bound to a channel — template code (unique, immutable), a display
 * name, a title / content body with {@code ${var}} placeholders.
 */

public class JulyOutboundTemplate {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Template code, unique and immutable.
     */
    private final String templateCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Template display name.
     */
    private String templateName;

    /**
     * Channel code the template is bound to.
     */
    private String channelCode;

    /**
     * Title with {@code ${var}} placeholders, optional.
     */
    private String title;

    /**
     * Content body with {@code ${var}} placeholders.
     */
    private String content;

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
     * @param templateCode template code, unique
     * @param sortOrder    manual sort order, null falls back to the default
     * @param templateName template display name
     * @param channelCode  channel code
     * @param title        title, optional
     * @param content      content body
     * @param status       row status
     * @param remark       remark, optional
     * @param audit        audit info
     */
    public JulyOutboundTemplate(EntityId id, String templateCode, Integer sortOrder, String templateName,
            String channelCode, String title, String content, String status, String remark, AuditInfo audit) {
        validate(templateCode, templateName, channelCode, content, title, remark);

        this.id = id;
        this.templateCode = templateCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.templateName = templateName;
        this.channelCode = channelCode;
        this.title = title;
        this.content = content;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new template.
     *
     * @param id           primary key
     * @param templateCode template code, unique, max 60
     * @param sortOrder    manual sort order, null falls back to the default
     * @param templateName template display name, max 100
     * @param channelCode  channel code, max 60
     * @param title        title, optional, max 300
     * @param content      content body
     * @param remark       remark, optional, max 300
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulyOutboundTemplate create(EntityId id, String templateCode, Integer sortOrder, String templateName,
            String channelCode, String title, String content, String remark, AuditInfo audit) {
        return new JulyOutboundTemplate(id, templateCode, sortOrder, templateName, channelCode, title, content,
                Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * Update the mutable fields (templateCode is immutable).
     *
     * @param templateName template display name
     * @param channelCode  channel code
     * @param title        title
     * @param content      content body
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param status       row status, null keeps the stored one
     * @param remark       remark
     */
    public void update(String templateName, String channelCode, String title, String content, Integer sortOrder,
            String status, String remark) {
        validate(this.templateCode, templateName, channelCode, content, title, remark);

        this.templateName = templateName;
        this.channelCode = channelCode;
        this.title = title;
        this.content = content;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the template fields.
     *
     * @param templateCode template code
     * @param templateName template display name
     * @param channelCode  channel code
     * @param content      content body
     * @param title        title
     * @param remark       remark
     */
    private static void validate(String templateCode, String templateName, String channelCode, String content,
            String title, String remark) {
        StringUtil011.requirePresent(templateCode, "template code", 60);
        StringUtil011.requirePresent(templateName, "template name", 100);
        StringUtil011.requirePresent(channelCode, "channel code", 60);
        StringUtil011.requirePresent(content, "content", 65535);
        StringUtil011.requireMax(title, "title", 300);
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
     * Get the template code.
     *
     * @return template code
     */
    public String templateCode() {
        return templateCode;
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
     * @return template name
     */
    public String templateName() {
        return templateName;
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
     * Get the title.
     *
     * @return title or null
     */
    public String title() {
        return title;
    }

    /**
     * Get the content body.
     *
     * @return content
     */
    public String content() {
        return content;
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

package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainPrompt class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt aggregate (child)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * AI prompt aggregate (child of {@link JulyAiDomain}): the concrete prompt
 * content mounted on one business domain. {@code inline} keeps the text in
 * {@code content}; {@code storage} keeps a pointer into object storage.
 */

public class JulyAiDomainPrompt {

    /** Inline content mode. */
    public static final String MODE_INLINE = "inline";

    /** Object-storage content mode. */
    public static final String MODE_STORAGE = "storage";

    /** Default sort order. */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /** Primary key. */
    private final EntityId id;

    /** Master id (pk_mt). */
    private final String pkMt;

    /** Prompt code, globally unique and immutable. */
    private final String promptCode;

    /** Prompt name. */
    private String promptName;

    /** Scene (inference / image / tts), classification only. */
    private String scene;

    /** Content mode (inline / storage). */
    private String contentMode;

    /** Inline content, nullable in storage mode. */
    private String content;

    /** Storage instance code, nullable in inline mode. */
    private String storageCode;

    /** Bucket, nullable in inline mode. */
    private String bucket;

    /** Object key, nullable in inline mode. */
    private String objectKey;

    /** Content hash, nullable. */
    private String contentHash;

    /** Content size, nullable. */
    private Long contentSize;

    /** Variable declarations, nullable. */
    private String variables;

    /** Sort order. */
    private Integer sortOrder;

    /** Row status. */
    private String status;

    /** Remark. */
    private String remark;

    /** Audit info. */
    private AuditInfo audit;

    /**
     * Full constructor (rehydration path).
     *
     * @param id          primary key
     * @param pkMt        master id
     * @param promptCode  prompt code
     * @param promptName  prompt name
     * @param scene       scene
     * @param contentMode content mode
     * @param content     inline content
     * @param storageCode storage instance code
     * @param bucket      bucket
     * @param objectKey   object key
     * @param contentHash content hash
     * @param contentSize content size
     * @param variables   variable declarations
     * @param sortOrder   sort order
     * @param status      row status
     * @param remark      remark
     * @param audit       audit info
     */
    public JulyAiDomainPrompt(EntityId id, String pkMt, String promptCode, String promptName, String scene,
            String contentMode, String content, String storageCode, String bucket, String objectKey,
            String contentHash, Long contentSize, String variables, Integer sortOrder, String status, String remark,
            AuditInfo audit) {
        validate(promptCode, promptName);

        this.id = id;
        this.pkMt = pkMt;
        this.promptCode = promptCode;
        this.promptName = promptName;
        this.scene = StringUtil011.isBlank(scene) ? "inference" : scene;
        this.contentMode = StringUtil011.isBlank(contentMode) ? MODE_INLINE : contentMode;
        this.content = content;
        this.storageCode = storageCode;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.contentHash = contentHash;
        this.contentSize = contentSize;
        this.variables = variables;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new prompt row.
     *
     * @param id          primary key
     * @param pkMt        master id
     * @param promptCode  prompt code
     * @param promptName  prompt name
     * @param scene       scene
     * @param contentMode content mode
     * @param content     inline content
     * @param variables   variable declarations
     * @param sortOrder   sort order
     * @param remark      remark
     * @param status      row status, null falls back to enabled
     * @param audit       audit info
     * @return new aggregate
     */
    public static JulyAiDomainPrompt create(EntityId id, String pkMt, String promptCode, String promptName,
            String scene, String contentMode, String content, String variables, Integer sortOrder, String remark,
            String status, AuditInfo audit) {
        return new JulyAiDomainPrompt(id, pkMt, promptCode, promptName, scene, contentMode, content, null, null, null,
                null, null, variables, sortOrder, status, remark, audit);
    }

    /**
     * Update the mutable fields (promptCode is immutable).
     *
     * @param promptName  prompt name
     * @param scene       scene
     * @param contentMode content mode
     * @param content     inline content
     * @param storageCode storage instance code
     * @param bucket      bucket
     * @param objectKey   object key
     * @param contentHash content hash
     * @param contentSize content size
     * @param variables   variable declarations
     * @param sortOrder   sort order, null keeps the stored one
     * @param remark      remark
     * @param status      row status, null keeps the stored one
     */
    public void update(String promptName, String scene, String contentMode, String content, String storageCode,
            String bucket, String objectKey, String contentHash, Long contentSize, String variables, Integer sortOrder,
            String remark, String status) {
        validate(this.promptCode, promptName);

        this.promptName = promptName;
        this.scene = StringUtil011.isBlank(scene) ? "inference" : scene;
        this.contentMode = StringUtil011.isBlank(contentMode) ? MODE_INLINE : contentMode;
        this.content = content;
        this.storageCode = storageCode;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.contentHash = contentHash;
        this.contentSize = contentSize;
        this.variables = variables;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Validate the required fields.
     *
     * @param promptCode prompt code
     * @param promptName prompt name
     */
    private static void validate(String promptCode, String promptName) {
        StringUtil011.requirePresent(promptCode, "prompt code", 60);
        StringUtil011.requirePresent(promptName, "prompt name", 100);
    }

    /** @return primary key */
    public EntityId id() {
        return id;
    }

    /** @return master id */
    public String pkMt() {
        return pkMt;
    }

    /** @return prompt code */
    public String promptCode() {
        return promptCode;
    }

    /** @return prompt name */
    public String promptName() {
        return promptName;
    }

    /** @return scene */
    public String scene() {
        return scene;
    }

    /** @return content mode */
    public String contentMode() {
        return contentMode;
    }

    /** @return inline content */
    public String content() {
        return content;
    }

    /** @return storage instance code */
    public String storageCode() {
        return storageCode;
    }

    /** @return bucket */
    public String bucket() {
        return bucket;
    }

    /** @return object key */
    public String objectKey() {
        return objectKey;
    }

    /** @return content hash */
    public String contentHash() {
        return contentHash;
    }

    /** @return content size */
    public Long contentSize() {
        return contentSize;
    }

    /** @return variable declarations */
    public String variables() {
        return variables;
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
}

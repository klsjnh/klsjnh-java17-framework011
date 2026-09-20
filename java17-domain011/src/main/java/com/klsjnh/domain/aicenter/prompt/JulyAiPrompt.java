package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiPrompt class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt aggregate (master)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * AI prompt aggregate root (master): the prompt's logical identity — a globally
 * unique code, a name and a scene. The per-domain content lives in the
 * {@link JulyAiPromptDetail} child aggregate.
 */

public class JulyAiPrompt {

    /** Default sort order. */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /** Primary key. */
    private final EntityId id;

    /** Prompt code, globally unique and immutable. */
    private final String promptCode;

    /** Prompt name. */
    private String promptName;

    /** Scene (inference / image / tts), classification only. */
    private String scene;

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
     * @param id         primary key
     * @param promptCode prompt code
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param status     row status
     * @param remark     remark
     * @param audit      audit info
     */
    public JulyAiPrompt(EntityId id, String promptCode, String promptName, String scene, Integer sortOrder,
            String status, String remark, AuditInfo audit) {
        validate(promptCode, promptName);

        this.id = id;
        this.promptCode = promptCode;
        this.promptName = promptName;
        this.scene = StringUtil011.isBlank(scene) ? "inference" : scene;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new prompt.
     *
     * @param id         primary key
     * @param promptCode prompt code
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param remark     remark
     * @param audit      audit info
     * @return new aggregate
     */
    public static JulyAiPrompt create(EntityId id, String promptCode, String promptName, String scene,
            Integer sortOrder, String remark, AuditInfo audit) {
        return new JulyAiPrompt(id, promptCode, promptName, scene, sortOrder, Status011.ENABLED.getCode(), remark,
                audit);
    }

    /**
     * Update the mutable fields (promptCode is immutable).
     *
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param remark     remark
     * @param status     row status, null keeps the stored one
     */
    public void update(String promptName, String scene, Integer sortOrder, String remark, String status) {
        validate(this.promptCode, promptName);

        this.promptName = promptName;
        this.scene = StringUtil011.isBlank(scene) ? "inference" : scene;
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

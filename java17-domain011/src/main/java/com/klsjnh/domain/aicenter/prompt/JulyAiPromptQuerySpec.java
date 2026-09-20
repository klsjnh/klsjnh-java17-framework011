package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiPromptQuerySpec record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt query spec
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Prompt page query condition: keyword (code / name), scene and status filters.
 *
 * @param keyword prompt code / name keyword (fuzzy), nullable
 * @param scene   scene filter, nullable
 * @param status  row status filter, nullable
 */

public record JulyAiPromptQuerySpec(String keyword, String scene, String status) {

    /**
     * Normalize the keyword (blank → null).
     *
     * @param keyword prompt code / name keyword (fuzzy), nullable
     * @param scene   scene filter, nullable
     * @param status  row status filter, nullable
     */
    public JulyAiPromptQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
        scene = StringUtil011.blankToNull(scene);
    }

    /** @return true when a keyword is present */
    public boolean hasKeyword() {
        return keyword != null;
    }

    /** @return true when a scene is present */
    public boolean hasScene() {
        return scene != null;
    }

    /** @return true when a status is present */
    public boolean hasStatus() {
        return !StringUtil011.isBlank(status);
    }
}

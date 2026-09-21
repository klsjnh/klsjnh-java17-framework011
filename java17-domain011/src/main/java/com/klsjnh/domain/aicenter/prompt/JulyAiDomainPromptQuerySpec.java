package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainPromptQuerySpec record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt query spec
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Prompt page query condition: keyword (code / name), master, scene and
 * status filters.
 *
 * @param keyword prompt code / name keyword (fuzzy), nullable
 * @param pkMt    master id filter, nullable
 * @param scene   scene filter, nullable
 * @param status  row status filter, nullable
 */

public record JulyAiDomainPromptQuerySpec(String keyword, String pkMt, String scene, String status) {

    /**
     * Normalize the filters (blank → null).
     *
     * @param keyword prompt code / name keyword (fuzzy), nullable
     * @param pkMt    master id filter, nullable
     * @param scene   scene filter, nullable
     * @param status  row status filter, nullable
     */
    public JulyAiDomainPromptQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
        pkMt = StringUtil011.blankToNull(pkMt);
        scene = StringUtil011.blankToNull(scene);
    }

    /** @return true when a keyword is present */
    public boolean hasKeyword() {
        return keyword != null;
    }

    /** @return true when a master id is present */
    public boolean hasPkMt() {
        return pkMt != null;
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

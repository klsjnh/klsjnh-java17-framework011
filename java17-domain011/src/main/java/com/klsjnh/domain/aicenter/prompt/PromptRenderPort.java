package com.klsjnh.domain.aicenter.prompt;

/*                PromptRenderPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  prompt render port (program entry for AI development)
 *
 */

import java.util.Map;

/**
 * Prompt render port: resolve a prompt by code (and optional domain), load its
 * content (inline or from object storage) and substitute {@code ${var}}
 * placeholders — the program entry for AI development.
 */

public interface PromptRenderPort {

    /**
     * Render a prompt by code.
     *
     * @param promptCode prompt code
     * @param domainCode business domain, blank for the default (first enabled)
     * @param params     variable values, nullable
     * @return rendered text
     */
    String render(String promptCode, String domainCode, Map<String, String> params);
}

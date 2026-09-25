package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainBundle class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain bundle class (master + children read view)
 *
 */

import java.util.List;

/**
 * Read result of a master-sub aggregate: a business domain together with its
 * prompts (sort order ascending), returned by the getWithChildren entry.
 *
 * @param domain  the domain aggregate (master)
 * @param prompts the prompts of the domain (children), ordered
 */

public record JulyAiDomainBundle(JulyAiDomain domain, List<JulyAiDomainPrompt> prompts) {
}

package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainPromptRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulyAiDomainPrompt} aggregate (child table).
 */

public interface JulyAiDomainPromptRepository {

    /**
     * Insert a new prompt row.
     *
     * @param prompt prompt row
     */
    void insert(JulyAiDomainPrompt prompt);

    /**
     * Update an existing prompt row.
     *
     * @param prompt prompt row with id
     */
    void update(JulyAiDomainPrompt prompt);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return prompt row or null
     */
    JulyAiDomainPrompt findById(String id);

    /**
     * Find by prompt code, enabled or not (globally unique).
     *
     * @param promptCode prompt code
     * @return prompt row or null
     */
    JulyAiDomainPrompt findByCode(String promptCode);

    /**
     * Find all enabled prompt rows of a domain, ordered.
     *
     * @param pkMt master id
     * @return prompt rows
     */
    List<JulyAiDomainPrompt> findByMaster(String pkMt);

    /**
     * Count rows of a domain.
     *
     * @param pkMt        master id
     * @param enabledOnly enabled only
     * @return row count
     */
    long countByMaster(String pkMt, boolean enabledOnly);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Logic delete all rows of a domain.
     *
     * @param pkMt master id
     */
    void logicDeleteByMaster(String pkMt);

    /**
     * Offset page query.
     *
     * @param offset   zero-based offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulyAiDomainPrompt> findPage(int offset, int pageSize, JulyAiDomainPromptQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total count
     */
    long count(JulyAiDomainPromptQuerySpec spec);
}

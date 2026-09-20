package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiPromptRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulyAiPrompt} aggregate.
 */

public interface JulyAiPromptRepository {

    /**
     * Insert a new prompt.
     *
     * @param prompt prompt
     */
    void insert(JulyAiPrompt prompt);

    /**
     * Update an existing prompt.
     *
     * @param prompt prompt with id
     */
    void update(JulyAiPrompt prompt);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return prompt or null
     */
    JulyAiPrompt findById(String id);

    /**
     * Find by prompt code, enabled or not.
     *
     * @param promptCode prompt code
     * @return prompt or null
     */
    JulyAiPrompt findByCode(String promptCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset page query.
     *
     * @param offset   zero-based offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulyAiPrompt> findPage(int offset, int pageSize, JulyAiPromptQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total count
     */
    long count(JulyAiPromptQuerySpec spec);
}

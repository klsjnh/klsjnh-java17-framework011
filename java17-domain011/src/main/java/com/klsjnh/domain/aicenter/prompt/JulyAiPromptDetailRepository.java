package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiPromptDetailRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt detail repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulyAiPromptDetail} aggregate (child table).
 */

public interface JulyAiPromptDetailRepository {

    /**
     * Insert a new detail row.
     *
     * @param detail detail row
     */
    void insert(JulyAiPromptDetail detail);

    /**
     * Update an existing detail row.
     *
     * @param detail detail row with id
     */
    void update(JulyAiPromptDetail detail);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return detail row or null
     */
    JulyAiPromptDetail findById(String id);

    /**
     * Find all enabled detail rows of a prompt, ordered.
     *
     * @param pkMt master id
     * @return detail rows
     */
    List<JulyAiPromptDetail> findByMaster(String pkMt);

    /**
     * Find the enabled detail row of a prompt for a domain.
     *
     * @param pkMt       master id
     * @param domainCode business domain
     * @return detail row or null
     */
    JulyAiPromptDetail findByDomain(String pkMt, String domainCode);

    /**
     * Count rows of a prompt.
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
     * Logic delete all rows of a prompt.
     *
     * @param pkMt master id
     */
    void logicDeleteByMaster(String pkMt);
}

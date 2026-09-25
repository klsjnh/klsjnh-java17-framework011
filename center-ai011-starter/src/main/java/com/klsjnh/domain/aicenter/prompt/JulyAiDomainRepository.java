package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulyAiDomain} aggregate (master tree).
 */

public interface JulyAiDomainRepository {

    /**
     * Insert a new domain.
     *
     * @param domain domain
     */
    void insert(JulyAiDomain domain);

    /**
     * Update an existing domain.
     *
     * @param domain domain with id
     */
    void update(JulyAiDomain domain);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return domain or null
     */
    JulyAiDomain findById(String id);

    /**
     * Find by domain code, enabled or not.
     *
     * @param domainCode domain code
     * @return domain or null
     */
    JulyAiDomain findByCode(String domainCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Whether a domain has alive children.
     *
     * @param parentId parent domain id
     * @return true when children exist
     */
    boolean hasChildren(String parentId);

    /**
     * Load the enabled domain tree (nested children, ordered by sort_order /
     * id). Assembly rules live in the persistence base (`TreeAssembler`).
     *
     * @return enabled domain roots with nested children
     */
    List<JulyAiDomain> findTree();

    /**
     * Offset page query.
     *
     * @param offset   zero-based offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulyAiDomain> findPage(int offset, int pageSize, JulyAiDomainQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total count
     */
    long count(JulyAiDomainQuerySpec spec);
}

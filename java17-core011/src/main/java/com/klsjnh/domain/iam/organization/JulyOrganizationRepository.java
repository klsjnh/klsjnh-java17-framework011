package com.klsjnh.domain.iam.organization;

/*                JulyOrganizationRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization repository interface
 *      2026.09.15  tree method renamed to getTree
 *
 */

import java.util.List;

/**
 * Repository port for the JulyOrganization aggregate (tree). selectTree
 * returns the full alive tree with children assembled.
 */

public interface JulyOrganizationRepository {

    /**
     * Insert a new aggregate.
     *
     * @param organization aggregate
     */
    void insert(JulyOrganization organization);

    /**
     * Update an existing aggregate.
     *
     * @param organization aggregate with id
     */
    void update(JulyOrganization organization);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyOrganization findById(String id);

    /**
     * Find by the unique organization code.
     *
     * @param orgCode organization code
     * @return aggregate or null
     */
    JulyOrganization findByCode(String orgCode);

    /**
     * Whether an organization has alive children.
     *
     * @param id organization id
     * @return true when children exist
     */
    boolean hasChildren(String id);

    /**
     * Find the alive children of one organization (flat).
     *
     * @param parentId parent organization id
     * @return child aggregates
     */
    List<JulyOrganization> findChildren(String parentId);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Load the full alive organization tree as aggregates (children
     * assembled, ordered by sort_order / id). Named getTree to stay
     * distinct from the Po-level base selectTree.
     *
     * @return root nodes with nested children
     */
    List<JulyOrganization> getTree();

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  org code / name keyword, nullable
     * @return page rows
     */
    List<JulyOrganization> findPage(int offset, int pageSize, String keyword);

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword org code / name keyword, nullable
     * @return total row count
     */
    long count(String keyword);
}

package com.klsjnh.domain.menu;

/*                JulyMenuRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyMenu aggregate (tree). selectTree returns the
 * full alive tree with children assembled.
 */

public interface JulyMenuRepository {

    /**
     * Insert a new aggregate.
     *
     * @param menu aggregate
     */
    void insert(JulyMenu menu);

    /**
     * Update an existing aggregate.
     *
     * @param menu aggregate with id
     */
    void update(JulyMenu menu);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyMenu findById(String id);

    /**
     * Find by the unique menu code.
     *
     * @param menuCode menu code
     * @return aggregate or null
     */
    JulyMenu findByCode(String menuCode);

    /**
     * Whether a menu has alive children.
     *
     * @param id menu id
     * @return true when children exist
     */
    boolean hasChildren(String id);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Load the full alive menu tree as aggregates (children assembled,
     * ordered by sort_order / id). Named selectMenuTree to stay distinct from
     * the Po-level base selectTree.
     *
     * @return root nodes with nested children
     */
    List<JulyMenu> selectMenuTree();

    /**
     * Find aggregates by a id list (flat, no children assembly).
     *
     * @param ids menu ids
     * @return aggregates present in the store
     */
    List<JulyMenu> findByIds(List<String> ids);

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  menu code / name keyword, nullable
     * @return page rows
     */
    List<JulyMenu> findPage(int offset, int pageSize, String keyword);

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword menu code / name keyword, nullable
     * @return total row count
     */
    long count(String keyword);
}

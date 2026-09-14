package com.klsjnh.domain.iam;

/*                JulyRoleRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyRole aggregate. Implemented in infrastructure on
 * MyBatis-Plus (master of the role_permissions child).
 */

public interface JulyRoleRepository {

    /**
     * Insert a new aggregate.
     *
     * @param role aggregate in enabled state
     */
    void insert(JulyRole role);

    /**
     * Update an existing aggregate.
     *
     * @param role aggregate with id
     */
    void update(JulyRole role);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyRole findById(String id);

    /**
     * Find by the unique role code.
     *
     * @param roleCode role code
     * @return aggregate or null
     */
    JulyRole findByCode(String roleCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  role code / name keyword, nullable
     * @return page rows
     */
    List<JulyRole> findPage(int offset, int pageSize, String keyword);

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword role code / name keyword, nullable
     * @return total row count
     */
    long count(String keyword);

    /**
     * Role codes of the given role ids (alive roles only).
     *
     * @param ids role ids
     * @return role code list
     */
    List<String> findCodesByIds(List<String> ids);
}

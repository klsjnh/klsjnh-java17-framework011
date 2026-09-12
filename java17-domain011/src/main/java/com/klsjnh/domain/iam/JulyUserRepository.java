package com.klsjnh.domain.iam;

/*                JulyUserRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyUser aggregate. Implemented in infrastructure on
 * MyBatis-Plus (master of the user_role / user_audit children).
 */

public interface JulyUserRepository {

    /**
     * Insert a new aggregate.
     *
     * @param user aggregate in enabled state
     */
    void insert(JulyUser user);

    /**
     * Update an existing aggregate.
     *
     * @param user aggregate with id
     */
    void update(JulyUser user);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyUser findById(String id);

    /**
     * Find by the unique login account.
     *
     * @param userAccount login account
     * @return aggregate or null
     */
    JulyUser findByAccount(String userAccount);

    /**
     * Cascade logic delete: toggle the user_role children, then the user.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset based page query with optional keyword filters.
     *
     * @param offset        zero-based row offset
     * @param pageSize      page size
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword   user name keyword, nullable
     * @return page rows
     */
    List<JulyUser> findPage(int offset, int pageSize, String accountKeyword, String nameKeyword);

    /**
     * Count with the same filters as findPage.
     *
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword    user name keyword, nullable
     * @return total row count
     */
    long count(String accountKeyword, String nameKeyword);

    /**
     * Refresh the last login time of a user.
     *
     * @param id user id
     */
    void touchLastLoginTime(String id);
}

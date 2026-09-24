package com.klsjnh.domain.iam.perm;

/*                JulyPermObjectRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission object catalog repository
 *
 */

import java.util.List;

/**
 * Repository port for the permission object catalog.
 */

public interface JulyPermObjectRepository {

    /**
     * Insert a new object.
     *
     * @param object aggregate
     */
    void insert(JulyPermObject object);

    /**
     * Find by object code (any status, not deleted).
     *
     * @param objectCode object code
     * @return aggregate or null
     */
    JulyPermObject findByCode(String objectCode);

    /**
     * Every enabled object, ordered by sort order.
     *
     * @return enabled aggregates
     */
    List<JulyPermObject> findAllEnabled();
}

package com.klsjnh.domain.iam.perm;

/*                JulyPermActionRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission action catalog repository
 *
 */

import java.util.List;

/**
 * Repository port for the permission action catalog.
 */

public interface JulyPermActionRepository {

    /**
     * Insert a new action.
     *
     * @param action aggregate
     */
    void insert(JulyPermAction action);

    /**
     * Find by full permission code.
     *
     * @param permissionCode permission code
     * @return aggregate or null
     */
    JulyPermAction findByPermissionCode(String permissionCode);

    /**
     * Find by object + action codes.
     *
     * @param objectCode object code
     * @param actionCode action code
     * @return aggregate or null
     */
    JulyPermAction findByObjectAndAction(String objectCode, String actionCode);

    /**
     * Enabled actions for one object, ordered by sort order.
     *
     * @param objectCode object code
     * @return enabled actions
     */
    List<JulyPermAction> findEnabledByObjectCode(String objectCode);
}

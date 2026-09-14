package com.klsjnh.infrastructure.system011.repository;

/*                JulyRolePermissionsRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role permissions repository impl class
 *
 */

import com.klsjnh.domain.system011.menu.JulyRolePermissionsRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.entity.JulyRolePermissionsPo;
import com.klsjnh.infrastructure.system011.mapper.JulyRolePermissionsMapper;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for the july_role_permissions junction: grant /
 * revoke use toggle semantics (revive an existing row, insert when absent).
 */

@Repository
public class JulyRolePermissionsRepositoryImpl
        extends BaseRepository<JulyRolePermissionsPo, JulyRolePermissionsMapper>
        implements JulyRolePermissionsRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyRolePermissionsRepositoryImpl(JulyRolePermissionsMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_role_permissions";
    }

    /**
     * Business unique column name.
     *
     * @return blank — the junction has no single business unique column
     */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /**
     * Grant one menu to a role: revive the row when it exists, insert
     * otherwise.
     *
     * @param pkMt           role id (master link)
     * @param pkMenu         menu id
     * @param permissionCode permission code snapshot, blank for pure visibility
     */
    @Override
    public void grant(String pkMt, String pkMenu, String permissionCode) {
        if (mapper.revive(pkMt, pkMenu, permissionCode) == 0) {
            JulyRolePermissionsPo po = new JulyRolePermissionsPo();
            po.setPkMt(pkMt);
            po.setPkMenu(pkMenu);
            po.setPermissionCode(permissionCode);
            mapper.insert(po);
        }
    }

    /**
     * Revoke all permission rows of one menu from a role.
     *
     * @param pkMt   role id (master link)
     * @param pkMenu menu id
     */
    @Override
    public void revokeByMenu(String pkMt, String pkMenu) {
        mapper.stopByMenu(pkMt, pkMenu);
    }

    /**
     * Menu ids currently granted to a role.
     *
     * @param pkMt role id (master link)
     * @return granted menu id list
     */
    @Override
    public List<String> findMenuIds(String pkMt) {
        return mapper.selectMenuIds(pkMt);
    }

    /**
     * Permission codes currently granted to a role.
     *
     * @param pkMt role id (master link)
     * @return permission code list
     */
    @Override
    public List<String> findPermissionCodes(String pkMt) {
        return mapper.selectPermissionCodes(pkMt);
    }
}

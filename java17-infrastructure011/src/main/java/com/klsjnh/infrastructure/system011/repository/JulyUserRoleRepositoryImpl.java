package com.klsjnh.infrastructure.system011.repository;

/*                JulyUserRoleRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user role repository impl class
 *
 */

import com.klsjnh.domain.iam.JulyUserRoleRepository;

import com.klsjnh.infrastructure.system011.entity.JulyUserRolePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.mapper.JulyUserRoleMapper;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for the july_user_role junction: assignment uses
 * toggle semantics (revive an existing row, insert when absent).
 */

@Repository
public class JulyUserRoleRepositoryImpl
        extends BaseRepository<JulyUserRolePo, JulyUserRoleMapper>
        implements JulyUserRoleRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyUserRoleRepositoryImpl(JulyUserRoleMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_user_role";
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
     * Assign a role to a user: revive the row when it exists, insert otherwise.
     *
     * @param pkMt   user id (master link)
     * @param pkRole role id
     */
    @Override
    public void assign(String pkMt, String pkRole) {
        if (mapper.revive(pkMt, pkRole) == 0) {
            JulyUserRolePo po = new JulyUserRolePo();
            po.setPkMt(pkMt);
            po.setPkRole(pkRole);
            mapper.insert(po);
        }
    }

    /**
     * Unassign a role from a user: set dr='1' on the row.
     *
     * @param pkMt   user id (master link)
     * @param pkRole role id
     */
    @Override
    public void unassign(String pkMt, String pkRole) {
        mapper.stop(pkMt, pkRole);
    }

    /**
     * Role ids currently granted to a user.
     *
     * @param pkMt user id (master link)
     * @return enabled role id list
     */
    @Override
    public List<String> findRoleIds(String pkMt) {
        return mapper.selectRoleIds(pkMt);
    }

    /**
     * User ids currently holding a role.
     *
     * @param pkRole role id
     * @return enabled user id list
     */
    @Override
    public List<String> findUserIds(String pkRole) {
        return mapper.selectUserIds(pkRole);
    }
}

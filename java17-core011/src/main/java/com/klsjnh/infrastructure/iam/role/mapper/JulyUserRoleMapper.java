package com.klsjnh.infrastructure.iam.role.mapper;

/*                JulyUserRoleMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user role mapper interface
 *
 */

import com.klsjnh.infrastructure.iam.role.entity.JulyUserRolePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MyBatis-Plus mapper for the july_user_role junction, plus toggle statements
 * for the assignment replace strategy (revive / stop by unique key — the
 * unique index covers dr='1' rows too, so delete+insert would collide).
 */

@Mapper
public interface JulyUserRoleMapper extends BaseMapper<JulyUserRolePo> {

    /**
     * Revive a junction row: set dr='0' by the unique key.
     *
     * @param pkMt   user id
     * @param pkRole role id
     * @return affected row count
     */
    @Update("UPDATE july_user_role SET dr = '0' WHERE pk_mt = #{pkMt} AND pk_role = #{pkRole}")
    int revive(@Param("pkMt") String pkMt, @Param("pkRole") String pkRole);

    /**
     * Stop a junction row: set dr='1' by the unique key.
     *
     * @param pkMt   user id
     * @param pkRole role id
     * @return affected row count
     */
    @Update("UPDATE july_user_role SET dr = '1' WHERE pk_mt = #{pkMt} AND pk_role = #{pkRole}")
    int stop(@Param("pkMt") String pkMt, @Param("pkRole") String pkRole);

    /**
     * Role ids currently granted to a user.
     *
     * @param pkMt user id
     * @return role id list
     */
    @Select("SELECT pk_role FROM july_user_role WHERE pk_mt = #{pkMt} AND dr = '0'")
    List<String> selectRoleIds(@Param("pkMt") String pkMt);

    /**
     * User ids currently holding a role.
     *
     * @param pkRole role id
     * @return user id list
     */
    @Select("SELECT pk_mt FROM july_user_role WHERE pk_role = #{pkRole} AND dr = '0'")
    List<String> selectUserIds(@Param("pkRole") String pkRole);
}

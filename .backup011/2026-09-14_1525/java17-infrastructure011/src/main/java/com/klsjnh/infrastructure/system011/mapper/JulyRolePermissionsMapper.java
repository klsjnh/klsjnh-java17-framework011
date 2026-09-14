package com.klsjnh.infrastructure.system011.mapper;

/*                JulyRolePermissionsMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role permissions mapper interface
 *
 */

import com.klsjnh.infrastructure.system011.entity.JulyRolePermissionsPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MyBatis-Plus mapper for the july_role_permissions junction, plus toggle
 * statements for the assignment replace strategy (the unique index covers
 * dr='1' rows too, so delete+insert would collide).
 */

@Mapper
public interface JulyRolePermissionsMapper extends BaseMapper<JulyRolePermissionsPo> {

    /**
     * Revive a junction row: set dr='0' by the unique key.
     *
     * @param pkMt           role id
     * @param pkMenu         menu id
     * @param permissionCode permission code snapshot
     * @return affected row count
     */
    @Update("UPDATE july_role_permissions SET dr = '0'"
            + " WHERE pk_mt = #{pkMt} AND pk_menu = #{pkMenu} AND permission_code = #{permissionCode}")
    int revive(@Param("pkMt") String pkMt, @Param("pkMenu") String pkMenu,
            @Param("permissionCode") String permissionCode);

    /**
     * Stop all permission rows of one menu from a role.
     *
     * @param pkMt   role id
     * @param pkMenu menu id
     * @return affected row count
     */
    @Update("UPDATE july_role_permissions SET dr = '1' WHERE pk_mt = #{pkMt} AND pk_menu = #{pkMenu}")
    int stopByMenu(@Param("pkMt") String pkMt, @Param("pkMenu") String pkMenu);

    /**
     * Menu ids currently granted to a role.
     *
     * @param pkMt role id
     * @return menu id list
     */
    @Select("SELECT pk_menu FROM july_role_permissions WHERE pk_mt = #{pkMt} AND dr = '0'")
    List<String> selectMenuIds(@Param("pkMt") String pkMt);

    /**
     * Permission codes currently granted to a role.
     *
     * @param pkMt role id
     * @return permission code list
     */
    @Select("SELECT permission_code FROM july_role_permissions WHERE pk_mt = #{pkMt} AND dr = '0'"
            + " AND permission_code != ''")
    List<String> selectPermissionCodes(@Param("pkMt") String pkMt);
}

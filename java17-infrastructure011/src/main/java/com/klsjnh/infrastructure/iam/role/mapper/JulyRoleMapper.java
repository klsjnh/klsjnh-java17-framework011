package com.klsjnh.infrastructure.iam.role.mapper;

/*                JulyRoleMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role mapper interface
 *
 */

import com.klsjnh.infrastructure.iam.role.entity.JulyRolePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_role table.
 */

@Mapper
public interface JulyRoleMapper extends BaseMapper<JulyRolePo> {
}

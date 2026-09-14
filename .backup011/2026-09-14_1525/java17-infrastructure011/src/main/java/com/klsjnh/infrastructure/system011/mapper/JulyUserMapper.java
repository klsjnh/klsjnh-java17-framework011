package com.klsjnh.infrastructure.system011.mapper;

/*                JulyUserMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user mapper interface
 *
 */

import com.klsjnh.infrastructure.system011.entity.JulyUserPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_user table.
 */

@Mapper
public interface JulyUserMapper extends BaseMapper<JulyUserPo> {
}

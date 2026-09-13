package com.klsjnh.infrastructure.system011.mapper;

/*                JulyOrganizationMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization mapper interface
 *
 */

import com.klsjnh.infrastructure.system011.entity.JulyOrganizationPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_organization table.
 */

@Mapper
public interface JulyOrganizationMapper extends BaseMapper<JulyOrganizationPo> {
}

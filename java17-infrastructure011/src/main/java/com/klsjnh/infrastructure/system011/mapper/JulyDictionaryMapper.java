package com.klsjnh.infrastructure.system011.mapper;

/*                JulyDictionaryMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary mapper interface
 *
 */

import com.klsjnh.infrastructure.system011.entity.JulyDictionaryPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_dictionary table.
 */

@Mapper
public interface JulyDictionaryMapper extends BaseMapper<JulyDictionaryPo> {
}

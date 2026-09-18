package com.klsjnh.infrastructure.datasource.mapper;

/*                JulyDatasourceMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource mapper interface
 *
 */

import com.klsjnh.infrastructure.datasource.entity.JulyDatasourcePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_datasource table.
 */

@Mapper
public interface JulyDatasourceMapper extends BaseMapper<JulyDatasourcePo> {
}

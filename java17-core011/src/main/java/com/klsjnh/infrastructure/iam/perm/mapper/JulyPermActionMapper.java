package com.klsjnh.infrastructure.iam.perm.mapper;

/*                JulyPermActionMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission action mapper
 *
 */

import com.klsjnh.infrastructure.iam.perm.entity.JulyPermActionPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for july_perm_action.
 */

@Mapper
public interface JulyPermActionMapper extends BaseMapper<JulyPermActionPo> {
}

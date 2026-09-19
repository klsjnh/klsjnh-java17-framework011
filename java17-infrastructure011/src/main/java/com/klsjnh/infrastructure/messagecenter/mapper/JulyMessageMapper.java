package com.klsjnh.infrastructure.messagecenter.mapper;

/*                JulyMessageMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message mapper interface
 *
 */

import com.klsjnh.infrastructure.messagecenter.entity.JulyMessagePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_message table.
 */

@Mapper
public interface JulyMessageMapper extends BaseMapper<JulyMessagePo> {
}

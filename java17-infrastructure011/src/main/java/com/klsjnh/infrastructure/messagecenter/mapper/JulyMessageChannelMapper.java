package com.klsjnh.infrastructure.messagecenter.mapper;

/*                JulyMessageChannelMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel mapper interface
 *
 */

import com.klsjnh.infrastructure.messagecenter.entity.JulyMessageChannelPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_message_channel table.
 */

@Mapper
public interface JulyMessageChannelMapper extends BaseMapper<JulyMessageChannelPo> {
}

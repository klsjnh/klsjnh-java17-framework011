package com.klsjnh.infrastructure.messagecenter.outbound.mapper;

/*                JulyOutboundTemplateMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template mapper interface
 *
 */

import com.klsjnh.infrastructure.messagecenter.outbound.entity.JulyOutboundTemplatePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_message_outbound_template table.
 */

@Mapper
public interface JulyOutboundTemplateMapper extends BaseMapper<JulyOutboundTemplatePo> {
}

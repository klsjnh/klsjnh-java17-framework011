package com.klsjnh.web.messagecenter.inbound.converter;

/*                JulyInboundChannelConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel converter class
 *
 */

import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;

import com.klsjnh.web.messagecenter.inbound.vo.channel.JulyInboundChannelVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyInboundChannel aggregate and the response VO.
 */

@Component
public class JulyInboundChannelConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param channel aggregate
     * @return response VO
     */
    public JulyInboundChannelVo011 toVo(JulyInboundChannel channel) {
        JulyInboundChannelVo011 vo = new JulyInboundChannelVo011();
        vo.setId(channel.id().value());
        vo.setChannelCode(channel.channelCode());
        vo.setSortOrder(channel.sortOrder());
        vo.setChannelName(channel.channelName());
        vo.setProviderType(channel.providerType());
        vo.setConfig(channel.config());
        vo.setStatus(channel.status());
        vo.setRemark(channel.remark());
        vo.setCreateBy(channel.audit().createBy());
        vo.setUpdateBy(channel.audit().updateBy());
        vo.setCreateTime(channel.audit().createTime());
        vo.setUpdateTime(channel.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param channels aggregates
     * @return response VO list
     */
    public List<JulyInboundChannelVo011> toVoList(List<JulyInboundChannel> channels) {
        List<JulyInboundChannelVo011> result = new ArrayList<>();

        for (JulyInboundChannel channel : channels) {
            result.add(toVo(channel));
        }

        return result;
    }
}

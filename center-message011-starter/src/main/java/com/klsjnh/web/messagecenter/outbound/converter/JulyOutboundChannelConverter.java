package com.klsjnh.web.messagecenter.outbound.converter;

/*                JulyOutboundChannelConverter class
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
 *      2026.09.26  mask sensitive config json fields in vo
 *
 */

import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannel;

import com.klsjnh.infrastructure.messagecenter.crypto.ChannelConfigCipher011;
import com.klsjnh.web.messagecenter.outbound.vo.channel.JulyOutboundChannelVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyOutboundChannel aggregate and the response VO.
 * Sensitive config JSON fields are masked ({@code ******}) and never echoed.
 */

@Component
public class JulyOutboundChannelConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param channel aggregate
     * @return response VO
     */
    public JulyOutboundChannelVo011 toVo(JulyOutboundChannel channel) {
        JulyOutboundChannelVo011 vo = new JulyOutboundChannelVo011();
        vo.setId(channel.id().value());
        vo.setChannelCode(channel.channelCode());
        vo.setSortOrder(channel.sortOrder());
        vo.setChannelName(channel.channelName());
        vo.setProviderType(channel.providerType());
        vo.setConfig(ChannelConfigCipher011.maskFields(channel.config()));
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
    public List<JulyOutboundChannelVo011> toVoList(List<JulyOutboundChannel> channels) {
        List<JulyOutboundChannelVo011> result = new ArrayList<>();

        for (JulyOutboundChannel channel : channels) {
            result.add(toVo(channel));
        }

        return result;
    }
}

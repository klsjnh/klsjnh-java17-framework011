package com.klsjnh.web.messagecenter.converter;

/*                JulyMessageChannelConverter class
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

import com.klsjnh.domain.messagecenter.channel.JulyMessageChannel;

import com.klsjnh.web.messagecenter.vo.channel.JulyMessageChannelVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyMessageChannel aggregate and the response VO.
 */

@Component
public class JulyMessageChannelConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param channel aggregate
     * @return response VO
     */
    public JulyMessageChannelVo011 toVo(JulyMessageChannel channel) {
        JulyMessageChannelVo011 vo = new JulyMessageChannelVo011();
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
    public List<JulyMessageChannelVo011> toVoList(List<JulyMessageChannel> channels) {
        List<JulyMessageChannelVo011> result = new ArrayList<>();

        for (JulyMessageChannel channel : channels) {
            result.add(toVo(channel));
        }

        return result;
    }
}

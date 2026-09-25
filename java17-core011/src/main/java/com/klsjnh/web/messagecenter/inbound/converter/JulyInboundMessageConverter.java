package com.klsjnh.web.messagecenter.inbound.converter;

/*                JulyInboundMessageConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message converter class
 *
 */

import com.klsjnh.application.messagecenter.inbound.message.MessageInboundResult;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessage;

import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundMessageVo011;
import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundReceiveResultVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyInboundMessage aggregate (and the receive result)
 * and the response VOs.
 */

@Component
public class JulyInboundMessageConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param message aggregate
     * @return response VO
     */
    public JulyInboundMessageVo011 toVo(JulyInboundMessage message) {
        JulyInboundMessageVo011 vo = new JulyInboundMessageVo011();
        vo.setId(message.id().value());
        vo.setChannelCode(message.channelCode());
        vo.setProviderType(message.providerType());
        vo.setMessageType(message.messageType());
        vo.setPayload(message.payload());
        vo.setFromId(message.fromId());
        vo.setContent(message.content());
        vo.setRawMessageId(message.rawMessageId());
        vo.setError(message.error());
        vo.setRemark(message.remark());
        vo.setStatus(message.status());
        vo.setCreateBy(message.audit().createBy());
        vo.setUpdateBy(message.audit().updateBy());
        vo.setCreateTime(message.audit().createTime());
        vo.setUpdateTime(message.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param messages aggregates
     * @return response VO list
     */
    public List<JulyInboundMessageVo011> toVoList(List<JulyInboundMessage> messages) {
        List<JulyInboundMessageVo011> result = new ArrayList<>();

        for (JulyInboundMessage message : messages) {
            result.add(toVo(message));
        }

        return result;
    }

    /**
     * Map a receive result to its response VO.
     *
     * @param result receive result
     * @return receive result VO
     */
    public JulyInboundReceiveResultVo011 toReceiveResultVo(MessageInboundResult result) {
        JulyInboundReceiveResultVo011 vo = new JulyInboundReceiveResultVo011();
        vo.setMessageId(result.messageId());
        vo.setDuplicate(result.duplicate());
        vo.setResponseBody(result.responseBody());

        return vo;
    }
}

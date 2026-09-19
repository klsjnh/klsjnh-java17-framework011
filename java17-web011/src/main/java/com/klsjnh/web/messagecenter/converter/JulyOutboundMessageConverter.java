package com.klsjnh.web.messagecenter.converter;

/*                JulyOutboundMessageConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message converter class
 *
 */

import com.klsjnh.application.messagecenter.outbound.message.MessageSendResult;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessage;

import com.klsjnh.web.messagecenter.vo.message.JulyOutboundMessageSendResultVo011;
import com.klsjnh.web.messagecenter.vo.message.JulyOutboundMessageVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyOutboundMessage aggregate (and the send result) and the
 * response VOs.
 */

@Component
public class JulyOutboundMessageConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param message aggregate
     * @return response VO
     */
    public JulyOutboundMessageVo011 toVo(JulyOutboundMessage message) {
        JulyOutboundMessageVo011 vo = new JulyOutboundMessageVo011();
        vo.setId(message.id().value());
        vo.setChannelCode(message.channelCode());
        vo.setProviderType(message.providerType());
        vo.setMessageType(message.messageType());
        vo.setPayload(message.payload());
        vo.setMsgTo(message.msgTo());
        vo.setTemplateCode(message.templateCode());
        vo.setTitle(message.title());
        vo.setContent(message.content());
        vo.setStatus(message.status());
        vo.setRetryCount(message.retryCount());
        vo.setError(message.error());
        vo.setRemark(message.remark());
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
    public List<JulyOutboundMessageVo011> toVoList(List<JulyOutboundMessage> messages) {
        List<JulyOutboundMessageVo011> result = new ArrayList<>();

        for (JulyOutboundMessage message : messages) {
            result.add(toVo(message));
        }

        return result;
    }

    /**
     * Map a send result to its response VO.
     *
     * @param result send result
     * @return send result VO
     */
    public JulyOutboundMessageSendResultVo011 toSendResultVo(MessageSendResult result) {
        JulyOutboundMessageSendResultVo011 vo = new JulyOutboundMessageSendResultVo011();
        vo.setMessageId(result.messageId());
        vo.setSuccess(result.success());
        vo.setChannelMessageId(result.channelMessageId());
        vo.setError(result.error());

        return vo;
    }
}

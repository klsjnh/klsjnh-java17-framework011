package com.klsjnh.web.messagecenter.inbound.converter;

/*                JulyInboundTemplateConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template converter class
 *
 */

import com.klsjnh.domain.messagecenter.inbound.template.JulyInboundTemplate;

import com.klsjnh.web.messagecenter.inbound.vo.template.JulyInboundTemplateVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyInboundTemplate aggregate and the response VO.
 */

@Component
public class JulyInboundTemplateConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param template aggregate
     * @return response VO
     */
    public JulyInboundTemplateVo011 toVo(JulyInboundTemplate template) {
        JulyInboundTemplateVo011 vo = new JulyInboundTemplateVo011();
        vo.setId(template.id().value());
        vo.setTemplateCode(template.templateCode());
        vo.setSortOrder(template.sortOrder());
        vo.setTemplateName(template.templateName());
        vo.setChannelCode(template.channelCode());
        vo.setTitle(template.title());
        vo.setContent(template.content());
        vo.setStatus(template.status());
        vo.setRemark(template.remark());
        vo.setCreateBy(template.audit().createBy());
        vo.setUpdateBy(template.audit().updateBy());
        vo.setCreateTime(template.audit().createTime());
        vo.setUpdateTime(template.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param templates aggregates
     * @return response VO list
     */
    public List<JulyInboundTemplateVo011> toVoList(List<JulyInboundTemplate> templates) {
        List<JulyInboundTemplateVo011> result = new ArrayList<>();

        for (JulyInboundTemplate template : templates) {
            result.add(toVo(template));
        }

        return result;
    }
}

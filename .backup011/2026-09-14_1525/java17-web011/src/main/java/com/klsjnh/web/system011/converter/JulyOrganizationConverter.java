package com.klsjnh.web.system011.converter;

/*                JulyOrganizationConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization converter class
 *
 */

import com.klsjnh.domain.system011.organization.JulyOrganization;
import com.klsjnh.web.system011.vo.julyorganization.JulyOrganizationVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converter between the JulyOrganization aggregate and the response VO
 * (recursive for tree nodes, member-count aware).
 */

@Component
public class JulyOrganizationConverter {

    /**
     * Map the aggregate to the response VO (children mapped recursively).
     *
     * @param organization aggregate
     * @return response VO
     */
    public JulyOrganizationVo011 toVo(JulyOrganization organization) {
        return toVo(organization, null);
    }

    /**
     * Map the aggregate to the response VO with a member-count badge.
     *
     * @param organization aggregate
     * @param counts       orgId → member count, nullable
     * @return response VO
     */
    public JulyOrganizationVo011 toVo(JulyOrganization organization, Map<String, Long> counts) {
        JulyOrganizationVo011 vo = new JulyOrganizationVo011();
        vo.setId(organization.id().value());
        vo.setParentId(organization.parentId());
        vo.setOrgCode(organization.orgCode());
        vo.setOrgName(organization.orgName());
        vo.setPkUser(organization.pkUser());
        vo.setOrgLevel(organization.orgLevel());
        vo.setSortOrder(organization.sortOrder());
        vo.setStatus(organization.status());

        if (counts != null) {
            vo.setMemberCount(counts.getOrDefault(organization.id().value(), 0L));
        }

        vo.setCreateBy(organization.audit().createBy());
        vo.setUpdateBy(organization.audit().updateBy());
        vo.setCreateTime(organization.audit().createTime());
        vo.setUpdateTime(organization.audit().updateTime());

        for (JulyOrganization child : organization.getChildren()) {
            vo.getChildren().add(toVo(child, counts));
        }

        return vo;
    }

    /**
     * Map aggregates to response VOs with member counts.
     *
     * @param organizations aggregates
     * @param counts        orgId → member count, nullable
     * @return response VO list
     */
    public List<JulyOrganizationVo011> toVoList(List<JulyOrganization> organizations, Map<String, Long> counts) {
        List<JulyOrganizationVo011> result = new ArrayList<>();

        for (JulyOrganization organization : organizations) {
            result.add(toVo(organization, counts));
        }

        return result;
    }
}

package com.klsjnh.web.iam.converter;

/*                JulyRoleConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role converter class
 *
 */

import com.klsjnh.domain.iam.role.JulyRole;

import com.klsjnh.web.iam.vo.julyrole.JulyRoleVo011;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter between the JulyRole aggregate and the response VO.
 */

@Component
public class JulyRoleConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param role aggregate
     * @return response VO
     */
    public JulyRoleVo011 toVo(JulyRole role) {
        JulyRoleVo011 vo = new JulyRoleVo011();
        vo.setId(role.id().value());
        vo.setRoleCode(role.roleCode());
        vo.setRoleName(role.roleName());
        vo.setIsBuiltin(role.isBuiltin());
        vo.setRemark(role.remark());
        vo.setStatus(role.status());
        vo.setCreateBy(role.audit().createBy());
        vo.setUpdateBy(role.audit().updateBy());
        vo.setCreateTime(role.audit().createTime());
        vo.setUpdateTime(role.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param roles aggregates
     * @return response VO list
     */
    public List<JulyRoleVo011> toVoList(List<JulyRole> roles) {
        return roles.stream().map(this::toVo).toList();
    }
}

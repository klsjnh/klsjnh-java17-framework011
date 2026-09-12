package com.klsjnh.web.system011.converter;

/*                JulyUserConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user converter class
 *
 */

import com.klsjnh.domain.iam.JulyUser;
import com.klsjnh.web.system011.vo.julyuser.JulyUserVo011;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter between the JulyUser aggregate and the response VO (the password
 * hash is never mapped).
 */

@Component
public class JulyUserConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param user aggregate
     * @return response VO
     */
    public JulyUserVo011 toVo(JulyUser user) {
        JulyUserVo011 vo = new JulyUserVo011();
        vo.setId(user.id().value());
        vo.setUserAccount(user.userAccount());
        vo.setUserName(user.userName());
        vo.setMobile(user.mobile());
        vo.setEmail(user.email());
        vo.setAvatar(user.avatar());
        vo.setPkOrg(user.pkOrg());
        vo.setLastLoginTime(user.lastLoginTime());
        vo.setStatus(user.status());
        vo.setCreateBy(user.audit().createBy());
        vo.setUpdateBy(user.audit().updateBy());
        vo.setCreateTime(user.audit().createTime());
        vo.setUpdateTime(user.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param users aggregates
     * @return response VO list
     */
    public List<JulyUserVo011> toVoList(List<JulyUser> users) {
        return users.stream().map(this::toVo).toList();
    }
}

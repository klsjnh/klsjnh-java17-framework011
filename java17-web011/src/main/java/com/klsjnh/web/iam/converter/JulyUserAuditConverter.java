package com.klsjnh.web.iam.converter;

/*                JulyUserAuditConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit converter class
 *
 */

import com.klsjnh.domain.iam.user.JulyUserAuditRow;

import com.klsjnh.web.iam.vo.julyuseraudit.JulyUserAuditVo011;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter between the audit read row and the response VO.
 */

@Component
public class JulyUserAuditConverter {

    /**
     * Map the read row to the response VO.
     *
     * @param row audit read row
     * @return response VO
     */
    public JulyUserAuditVo011 toVo(JulyUserAuditRow row) {
        JulyUserAuditVo011 vo = new JulyUserAuditVo011();
        vo.setId(row.id());
        vo.setPkMt(row.pkMt());
        vo.setUserAccount(row.userAccount());
        vo.setAuditType(row.auditType());
        vo.setObjectCode(row.objectCode());
        vo.setAuditContent(row.auditContent());
        vo.setAuditIp(row.auditIp());
        vo.setCreateTime(row.createTime());

        return vo;
    }

    /**
     * Map read rows to response VOs.
     *
     * @param rows audit read rows
     * @return response VO list
     */
    public List<JulyUserAuditVo011> toVoList(List<JulyUserAuditRow> rows) {
        return rows.stream().map(this::toVo).toList();
    }
}

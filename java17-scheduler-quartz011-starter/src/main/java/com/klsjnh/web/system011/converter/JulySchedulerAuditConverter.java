package com.klsjnh.web.system011.converter;

/*                JulySchedulerAuditConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit converter class
 *
 */

import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRow;

import com.klsjnh.web.system011.vo.julyscheduler.JulySchedulerAuditVo011;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter between the execution-audit read row and the response VO.
 */

@Component
public class JulySchedulerAuditConverter {

    /**
     * Map the read row to the response VO.
     *
     * @param row audit read row
     * @return response VO
     */
    public JulySchedulerAuditVo011 toVo(JulySchedulerAuditRow row) {
        JulySchedulerAuditVo011 vo = new JulySchedulerAuditVo011();
        vo.setId(row.id());
        vo.setPkMt(row.pkMt());
        vo.setSchedulerCode(row.schedulerCode());
        vo.setStartTime(row.startTime());
        vo.setEndTime(row.endTime());
        vo.setExecStatus(row.execStatus());
        vo.setErrorMessage(row.errorMessage());
        vo.setCreateTime(row.createTime());

        return vo;
    }

    /**
     * Map read rows to response VOs.
     *
     * @param rows audit read rows
     * @return response VO list
     */
    public List<JulySchedulerAuditVo011> toVoList(List<JulySchedulerAuditRow> rows) {
        return rows.stream().map(this::toVo).toList();
    }
}

package com.klsjnh.web.system011.converter;

/*                JulySchedulerConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler converter class
 *
 */

import com.klsjnh.domain.system011.scheduler.JulyScheduler;

import com.klsjnh.web.system011.vo.JulySchedulerVo011;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter between the JulyScheduler aggregate and the response VO. The
 * controller stays request-in / response-out only.
 */

@Component
public class JulySchedulerConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param scheduler aggregate
     * @return response VO
     */
    public JulySchedulerVo011 toVo(JulyScheduler scheduler) {
        JulySchedulerVo011 vo = new JulySchedulerVo011();
        vo.setId(scheduler.id().value());
        vo.setSchedulerCode(scheduler.schedulerCode());
        vo.setSchedulerName(scheduler.schedulerName());
        vo.setSchedulerHandler(scheduler.schedulerHandler());
        vo.setSchedulerCron(scheduler.schedulerCron());
        vo.setExecuteTimes(scheduler.executeTimes());
        vo.setStatus(scheduler.status());
        vo.setCreateBy(scheduler.audit().createBy());
        vo.setUpdateBy(scheduler.audit().updateBy());
        vo.setCreateTime(scheduler.audit().createTime());
        vo.setUpdateTime(scheduler.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param schedulers aggregates
     * @return response VO list
     */
    public List<JulySchedulerVo011> toVoList(List<JulyScheduler> schedulers) {
        return schedulers.stream().map(this::toVo).toList();
    }
}

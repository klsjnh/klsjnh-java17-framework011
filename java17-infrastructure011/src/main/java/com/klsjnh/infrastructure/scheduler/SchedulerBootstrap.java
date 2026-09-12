package com.klsjnh.infrastructure.scheduler;

/*                SchedulerBootstrap class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  scheduler bootstrap class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.Status011;

import com.klsjnh.infrastructure.system011.entity.JulySchedulerPo;
import com.klsjnh.infrastructure.system011.mapper.JulySchedulerMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Re-registers running tasks into the Quartz engine after startup
 * (self-healing: the july_scheduler table is the source of truth).
 */

@Component
public class SchedulerBootstrap {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SchedulerBootstrap.class);

    /**
     * Mapper for the running task query.
     */
    private final JulySchedulerMapper mapper;

    /**
     * Scheduler engine.
     */
    private final SchedulerEngine engine;

    /**
     * Create the bootstrap.
     *
     * @param mapper july scheduler mapper
     * @param engine scheduler engine
     */
    public SchedulerBootstrap(JulySchedulerMapper mapper, SchedulerEngine engine) {
        this.mapper = mapper;
        this.engine = engine;
    }

    /**
     * Re-register running tasks after the application is ready.
     *
     * @param event application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reRegister(ApplicationReadyEvent event) {
        String funcName = "re register";

        List<JulySchedulerPo> running = mapper.selectList(new QueryWrapper<JulySchedulerPo>()
                .eq("status", Status011.ENABLED.getCode()));

        for (JulySchedulerPo po : running) {
            try {
                engine.register(po.getId(), po.getSchedulerHandler(), po.getSchedulerCron());
            } catch (Exception ex) {
                logger.error("{} id {} failed {} ...", funcName, po.getId(), ex.getMessage());
            }
        }

        logger.info("{} {} running tasks registered ...", funcName, running.size());
    }
}

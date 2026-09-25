package com.klsjnh.infrastructure.system011.scheduler.mapper;

/*                JulySchedulerAuditMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit mapper interface
 *
 */

import com.klsjnh.infrastructure.system011.scheduler.entity.JulySchedulerAuditPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for july_scheduler_audit (insert + query by design;
 * no update / delete API surface).
 */

@Mapper
public interface JulySchedulerAuditMapper extends BaseMapper<JulySchedulerAuditPo> {
}

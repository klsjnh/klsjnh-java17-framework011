package com.klsjnh.infrastructure.iam.user.mapper;

/*                JulyUserAuditMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user audit mapper interface
 *
 */

import com.klsjnh.infrastructure.iam.user.entity.JulyUserAuditPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_user_audit table (insert only by design).
 */

@Mapper
public interface JulyUserAuditMapper extends BaseMapper<JulyUserAuditPo> {
}

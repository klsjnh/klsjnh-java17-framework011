package com.klsjnh.infrastructure.ai011.mapper;

/*                AiModelProviderMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider mapper interface
 *
 */

import com.klsjnh.infrastructure.ai011.entity.AiModelProviderPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_ai_model_provider table.
 */

@Mapper
public interface AiModelProviderMapper extends BaseMapper<AiModelProviderPo> {
}

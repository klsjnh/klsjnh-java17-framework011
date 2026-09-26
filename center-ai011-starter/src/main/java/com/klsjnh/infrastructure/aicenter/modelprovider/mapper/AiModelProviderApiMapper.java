package com.klsjnh.infrastructure.aicenter.modelprovider.mapper;

/*                AiModelProviderApiMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api mapper interface
 *
 */

import com.klsjnh.infrastructure.aicenter.modelprovider.entity.AiModelProviderApiPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_ai_model_provider_api table.
 */

@Mapper
public interface AiModelProviderApiMapper extends BaseMapper<AiModelProviderApiPo> {
}

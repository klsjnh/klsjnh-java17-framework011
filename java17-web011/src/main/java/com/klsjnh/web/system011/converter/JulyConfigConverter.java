package com.klsjnh.web.system011.converter;

/*                JulyConfigConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config converter class
 *
 */

import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyConfig aggregate and the response VO.
 */

@Component
public class JulyConfigConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param config aggregate
     * @return response VO
     */
    public JulyConfigVo011 toVo(JulyConfig config) {
        JulyConfigVo011 vo = new JulyConfigVo011();
        vo.setId(config.id().value());
        vo.setCode(config.code());
        vo.setData(config.data());
        vo.setStatus(config.status());
        vo.setCreateBy(config.audit().createBy());
        vo.setUpdateBy(config.audit().updateBy());
        vo.setCreateTime(config.audit().createTime());
        vo.setUpdateTime(config.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param configs aggregates
     * @return response VO list
     */
    public List<JulyConfigVo011> toVoList(List<JulyConfig> configs) {
        List<JulyConfigVo011> result = new ArrayList<>();

        for (JulyConfig config : configs) {
            result.add(toVo(config));
        }

        return result;
    }
}

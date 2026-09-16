package com.klsjnh.web.ai011.converter;

/*                AiModelProviderConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider converter class
 *
 */

import com.klsjnh.domain.ai011.AiModelProbePort;
import com.klsjnh.domain.ai011.AiModelProvider;
import com.klsjnh.domain.ai011.AiModelProviderApi;

import com.klsjnh.web.ai011.vo.aimodelprovider.AiModelProviderApiVo011;
import com.klsjnh.web.ai011.vo.aimodelprovider.AiModelProviderTestResultVo011;
import com.klsjnh.web.ai011.vo.aimodelprovider.AiModelProviderVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the ai011 aggregates and the response VOs.
 * <p>
 * This class is the write-only boundary of the api key: the target
 * {@link AiModelProviderApiVo011} simply has no such field, so there is no code
 * path here that could leak it.
 * </p>
 */

@Component
public class AiModelProviderConverter {

    /**
     * Map the provider aggregate to the response VO.
     *
     * @param provider aggregate
     * @return response VO
     */
    public AiModelProviderVo011 toVo(AiModelProvider provider) {
        AiModelProviderVo011 vo = new AiModelProviderVo011();
        vo.setId(provider.id().value());
        vo.setProviderCode(provider.providerCode());
        vo.setSortOrder(provider.sortOrder());
        vo.setProviderName(provider.providerName());
        vo.setBaseUrl(provider.baseUrl());
        vo.setModels(provider.models());
        vo.setStatus(provider.status());
        vo.setRemark(provider.remark());
        vo.setCreateBy(provider.audit().createBy());
        vo.setUpdateBy(provider.audit().updateBy());
        vo.setCreateTime(provider.audit().createTime());
        vo.setUpdateTime(provider.audit().updateTime());

        return vo;
    }

    /**
     * Map provider aggregates to response VOs.
     *
     * @param providers aggregates
     * @return response VO list
     */
    public List<AiModelProviderVo011> toVoList(List<AiModelProvider> providers) {
        List<AiModelProviderVo011> result = new ArrayList<>();

        for (AiModelProvider provider : providers) {
            result.add(toVo(provider));
        }

        return result;
    }

    /**
     * Map the api key entity to the response VO (without the key).
     *
     * @param api entity
     * @return response VO (no apiKey field)
     */
    public AiModelProviderApiVo011 toApiVo(AiModelProviderApi api) {
        AiModelProviderApiVo011 vo = new AiModelProviderApiVo011();
        vo.setId(api.id().value());
        vo.setApiCode(api.apiCode());
        vo.setApiName(api.apiName());
        vo.setSortOrder(api.sortOrder());
        vo.setStatus(api.status());
        vo.setRemark(api.remark());

        return vo;
    }

    /**
     * Map api key entities to response VOs.
     *
     * @param apis entities
     * @return response VO list
     */
    public List<AiModelProviderApiVo011> toApiVoList(List<AiModelProviderApi> apis) {
        List<AiModelProviderApiVo011> result = new ArrayList<>();

        for (AiModelProviderApi api : apis) {
            result.add(toApiVo(api));
        }

        return result;
    }

    /**
     * Map a probe result to its response VO.
     *
     * @param result probe result
     * @return test result VO
     */
    public AiModelProviderTestResultVo011 toTestResultVo(AiModelProbePort.ProbeResult result) {
        AiModelProviderTestResultVo011 vo = new AiModelProviderTestResultVo011();
        vo.setSuccess(result.success());
        vo.setMessage(result.message());
        vo.setHttpStatus(result.httpStatus());

        return vo;
    }
}

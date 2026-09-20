package com.klsjnh.application.aicenter.modelprovider;

/*                AiModelProviderUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProbePort;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApiRepository;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderQuerySpec;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AiModelProvider use cases: provider and api key CRUD, the program read
 * entries (getByCode / getApiList / getDefaultApi) and the connectivity probe
 * at provider and api level.
 */

@Service
public class AiModelProviderUseCase {

    /**
     * Provider repository.
     */
    private final AiModelProviderRepository providerRepository;

    /**
     * Api key repository.
     */
    private final AiModelProviderApiRepository apiRepository;

    /**
     * Connectivity probe.
     */
    private final AiModelProbePort probePort;

    /**
     * Create the use case.
     *
     * @param providerRepository provider repository
     * @param apiRepository      api key repository
     * @param probePort          connectivity probe
     */
    public AiModelProviderUseCase(AiModelProviderRepository providerRepository,
            AiModelProviderApiRepository apiRepository, AiModelProbePort probePort) {
        this.providerRepository = providerRepository;
        this.apiRepository = apiRepository;
        this.probePort = probePort;
    }

    /**
     * Insert a new provider.
     *
     * @param providerCode provider code, unique, immutable
     * @param sortOrder    manual sort order, null falls back to the default
     * @param providerName provider display name
     * @param baseUrl      base url
     * @param models       comma separated model list, optional
     * @param remark       remark, optional
     * @return new provider id
     */
    @Transactional
    public String insert(String providerCode, Integer sortOrder, String providerName, String baseUrl, String models,
            String remark) {
        if (providerRepository.findByCode(providerCode) != null) {
            throw BusinessException.badRequest("provider code already exists: " + providerCode);
        }

        AiModelProvider provider = newProvider(providerCode, sortOrder, providerName, baseUrl, models, remark);
        providerRepository.insert(provider);

        return provider.id().value();
    }

    /**
     * Update a provider.
     *
     * @param id           provider id
     * @param providerName provider display name
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param baseUrl      base url
     * @param models       comma separated model list, optional
     * @param status       row status, null keeps the stored one
     * @param remark       remark, optional
     * @return provider id
     */
    @Transactional
    public String update(String id, String providerName, Integer sortOrder, String baseUrl, String models, String status,
            String remark) {
        AiModelProvider provider = requireProvider(id);
        requireStatus(status);
        applyProviderUpdate(provider, providerName, sortOrder, baseUrl, models, status, remark);
        providerRepository.update(provider);

        return provider.id().value();
    }

    /**
     * Logic delete a provider; refused while it still has api keys.
     *
     * @param id provider id
     * @return deleted provider id
     */
    @Transactional
    public String logicDelete(String id) {
        AiModelProvider provider = requireProvider(id);

        if (apiRepository.countByMaster(provider.id().value()) > 0) {
            throw BusinessException.badRequest("provider still has api keys, delete them first: " + id);
        }

        if (!providerRepository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Find a provider by primary key.
     *
     * @param id provider id
     * @return aggregate
     */
    public AiModelProvider getById(String id) {
        return requireProvider(id);
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<AiModelProvider> selectListByPage(PageQuery011 pageQuery, AiModelProviderQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        AiModelProviderQuerySpec condition = spec == null ? new AiModelProviderQuerySpec(null, null) : spec;
        List<AiModelProvider> rows = providerRepository.findPage(query.offset(), query.pageSize(), condition);
        long total = providerRepository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Insert a new api key under a provider.
     *
     * @param providerCode provider code locating the master
     * @param sortOrder    manual sort order, null falls back to the default
     * @param apiCode      api key code, unique within the provider
     * @param apiName      api key display name
     * @param apiKey       api key secret
     * @param remark       remark, optional
     * @return new api key id
     */
    @Transactional
    public String insertApi(String providerCode, Integer sortOrder, String apiCode, String apiName, String apiKey,
            String remark) {
        AiModelProvider provider = providerRepository.findByCode(providerCode);

        if (provider == null) {
            throw BusinessException.recordNotFound(providerCode);
        }

        String providerId = provider.id().value();

        if (apiRepository.findByMasterAndCode(providerId, apiCode) != null) {
            throw BusinessException.badRequest("api code already exists in provider: " + apiCode);
        }

        AiModelProviderApi api = newApi(providerId, sortOrder, apiCode, apiName, apiKey, remark);
        apiRepository.insert(api);

        return api.id().value();
    }

    /**
     * Update an api key; a blank apiKey keeps the stored one.
     *
     * @param id        api key id
     * @param apiName   api key display name
     * @param sortOrder manual sort order, null keeps the stored one
     * @param apiKey    api key secret, blank keeps the stored one
     * @param status    row status, null keeps the stored one
     * @param remark    remark, optional
     * @return api key id
     */
    @Transactional
    public String updateApi(String id, String apiName, Integer sortOrder, String apiKey, String status, String remark) {
        AiModelProviderApi api = requireApi(id);
        requireStatus(status);
        applyApiUpdate(api, apiName, sortOrder, apiKey, status, remark);
        apiRepository.update(api);

        return api.id().value();
    }

    /**
     * Logic delete an api key.
     *
     * @param id api key id
     * @return deleted api key id
     */
    @Transactional
    public String logicDeleteApi(String id) {
        if (!apiRepository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * List the api keys of a provider (management view, no key content).
     *
     * @param providerCode provider code
     * @param status       optional status filter, null for all
     * @return ordered entities, never null
     */
    public List<AiModelProviderApi> selectApiListByProvider(String providerCode, String status) {
        AiModelProvider provider = requireProviderByCode(providerCode);

        return apiRepository.findAllByMaster(provider.id().value(), status);
    }

    /**
     * Program read entry: the ENABLED provider of a code, null when missing or
     * disabled. Readers query the store every time (no cache).
     *
     * @param providerCode provider code
     * @return aggregate or null
     */
    public AiModelProvider getByCode(String providerCode) {
        return providerRepository.findEnabledByCode(providerCode);
    }

    /**
     * Program read entry: the ENABLED keys of a provider, sort order ascending.
     *
     * @param providerCode provider code
     * @return entities, never null
     */
    public List<AiModelProviderApi> getApiList(String providerCode) {
        AiModelProvider provider = getByCode(providerCode);

        if (provider == null) {
            return List.of();
        }

        return apiRepository.findByMaster(provider.id().value());
    }

    /**
     * Program read entry: the first ENABLED key of a provider (sort order
     * ascending), null when none.
     *
     * @param providerCode provider code
     * @return entity or null
     */
    public AiModelProviderApi getDefaultApi(String providerCode) {
        List<AiModelProviderApi> apis = getApiList(providerCode);

        return apis.isEmpty() ? null : apis.get(0);
    }

    /**
     * Probe a provider with its default enabled api key.
     *
     * @param providerCode provider code
     * @return probe result, never null
     */
    public AiModelProbePort.ProbeResult testConnection(String providerCode) {
        AiModelProvider provider = requireProviderByCode(providerCode);
        List<AiModelProviderApi> apis = apiRepository.findByMaster(provider.id().value());

        if (apis.isEmpty()) {
            return new AiModelProbePort.ProbeResult(false, "no enabled api key", null);
        }

        return probePort.probe(provider.baseUrl(), apis.get(0).apiKey());
    }

    /**
     * Probe a specific api key (sub-table level).
     *
     * @param id api key id
     * @return probe result, never null
     */
    public AiModelProbePort.ProbeResult testConnectionApi(String id) {
        AiModelProviderApi api = requireApi(id);
        AiModelProvider provider = requireProvider(api.pkMt());

        return probePort.probe(provider.baseUrl(), api.apiKey());
    }

    /**
     * Apply an update on the provider aggregate, translating domain validation
     * failures into 400 responses.
     *
     * @param provider     aggregate
     * @param providerName provider display name
     * @param sortOrder    manual sort order
     * @param baseUrl      base url
     * @param models       comma separated model list
     * @param status       row status
     * @param remark       remark
     */
    private void applyProviderUpdate(AiModelProvider provider, String providerName, Integer sortOrder, String baseUrl,
            String models, String status, String remark) {
        try {
            provider.update(providerName, sortOrder, baseUrl, models, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Apply an update on the api key entity, translating domain validation
     * failures into 400 responses.
     *
     * @param api       entity
     * @param apiName   api key display name
     * @param sortOrder manual sort order
     * @param apiKey    api key secret
     * @param status    row status
     * @param remark    remark
     */
    private void applyApiUpdate(AiModelProviderApi api, String apiName, Integer sortOrder, String apiKey, String status,
            String remark) {
        try {
            api.update(apiName, sortOrder, apiKey, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new provider aggregate, translating domain validation failures
     * into 400 responses.
     *
     * @param providerCode provider code
     * @param sortOrder    manual sort order
     * @param providerName provider display name
     * @param baseUrl      base url
     * @param models       comma separated model list
     * @param remark       remark
     * @return new aggregate
     */
    private AiModelProvider newProvider(String providerCode, Integer sortOrder, String providerName, String baseUrl,
            String models, String remark) {
        try {
            return AiModelProvider.create(EntityId.generate(), providerCode, sortOrder, providerName, baseUrl, models,
                    remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new api key entity, translating domain validation failures into
     * 400 responses.
     *
     * @param providerId provider id
     * @param sortOrder  manual sort order
     * @param apiCode    api key code
     * @param apiName    api key display name
     * @param apiKey     api key secret
     * @param remark     remark
     * @return new entity
     */
    private AiModelProviderApi newApi(String providerId, Integer sortOrder, String apiCode, String apiName, String apiKey,
            String remark) {
        try {
            return AiModelProviderApi.create(EntityId.generate(), providerId, sortOrder, apiCode, apiName, apiKey,
                    remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing provider by id.
     *
     * @param id provider id
     * @return aggregate
     */
    private AiModelProvider requireProvider(String id) {
        AiModelProvider provider = providerRepository.findById(id);

        if (provider == null) {
            throw BusinessException.recordNotFound(id);
        }

        return provider;
    }

    /**
     * Require an existing provider by code (enabled or not).
     *
     * @param providerCode provider code
     * @return aggregate
     */
    private AiModelProvider requireProviderByCode(String providerCode) {
        AiModelProvider provider = providerRepository.findByCode(providerCode);

        if (provider == null) {
            throw BusinessException.recordNotFound(providerCode);
        }

        return provider;
    }

    /**
     * Require an existing api key.
     *
     * @param id api key id
     * @return entity
     */
    private AiModelProviderApi requireApi(String id) {
        AiModelProviderApi api = apiRepository.findById(id);

        if (api == null) {
            throw BusinessException.recordNotFound(id);
        }

        return api;
    }

    /**
     * Reject an unknown status when one is supplied.
     *
     * @param status raw status, nullable
     */
    private void requireStatus(String status) {
        if (status != null && !status.isBlank() && Status011.of(status) == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }
    }
}

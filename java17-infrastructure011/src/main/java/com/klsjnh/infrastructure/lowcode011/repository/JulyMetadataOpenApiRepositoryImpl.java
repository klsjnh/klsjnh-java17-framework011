package com.klsjnh.infrastructure.lowcode011.repository;

/*                JulyMetadataOpenApiRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  july metadata open api repository impl class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataOpenApi;
import com.klsjnh.domain.lowcode011.JulyMetadataOpenApiRepository;

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataOpenApiPo;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataOpenApiMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

/**
 * Repository implementation for the open API configuration (one row per
 * object).
 */

@Repository
public class JulyMetadataOpenApiRepositoryImpl implements JulyMetadataOpenApiRepository {

    /**
     * Configuration mapper.
     */
    private final JulyMetadataOpenApiMapper mapper;

    /**
     * Create the repository.
     *
     * @param mapper configuration mapper
     */
    public JulyMetadataOpenApiRepositoryImpl(JulyMetadataOpenApiMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Find the configuration of an object.
     *
     * @param objectName object name
     * @return configuration or null
     */
    @Override
    public JulyMetadataOpenApi findByObjectName(String objectName) {
        return toDomain(mapper.selectOne(new QueryWrapper<JulyMetadataOpenApiPo>().eq("object_name", objectName)));
    }

    /**
     * Insert a configuration.
     *
     * @param config configuration
     */
    @Override
    public void insert(JulyMetadataOpenApi config) {
        JulyMetadataOpenApiPo po = new JulyMetadataOpenApiPo();
        po.setId(config.id());
        po.setStatus("1");
        po.setObjectName(config.objectName());
        po.setEnabled(config.enabled() ? "1" : "0");
        po.setAuthMode(config.authMode());
        po.setAllowedOps(config.allowedOps());
        po.setApiKey(config.apiKey());
        po.setApiKeyUpdatedAt(config.apiKeyUpdatedAt());

        mapper.insert(po);
    }

    /**
     * Update a configuration by object name.
     *
     * @param config configuration
     */
    @Override
    public void update(JulyMetadataOpenApi config) {
        mapper.update(null, new UpdateWrapper<JulyMetadataOpenApiPo>()
                .eq("object_name", config.objectName())
                .set("enabled", config.enabled() ? "1" : "0")
                .set("auth_mode", config.authMode())
                .set("allowed_ops", config.allowedOps())
                .set("api_key", config.apiKey())
                .set("api_key_updated_at", config.apiKeyUpdatedAt()));
    }

    /**
     * Map a PO to the domain record.
     *
     * @param po persistence object, nullable
     * @return domain configuration or null
     */
    private JulyMetadataOpenApi toDomain(JulyMetadataOpenApiPo po) {
        if (po == null) {
            return null;
        }

        return new JulyMetadataOpenApi(po.getId(), po.getObjectName(), "1".equals(po.getEnabled()), po.getAuthMode(),
                po.getAllowedOps(), po.getApiKey(), po.getApiKeyUpdatedAt());
    }
}

package com.klsjnh.application.messagecenter.outbound.channel;

/*                JulyOutboundChannelUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel use case class
 *      2026.09.26  explicit permission checks (julyMessageOutboundChannel)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannel;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyMessageOutboundChannelPermissionCodes011;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannelQuerySpec;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannelRepository;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.crypto.ChannelConfigCipher011;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyOutboundChannel use cases: channel configuration CRUD.
 */

@Service
public class JulyOutboundChannelUseCase {

    /**
     * Channel repository.
     */
    private final JulyOutboundChannelRepository repository;

    private final AuthorizationPort authorizationPort;

    public JulyOutboundChannelUseCase(JulyOutboundChannelRepository repository,
            AuthorizationPort authorizationPort) {
        this.repository = repository;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Insert a new channel.
     *
     * @param channelCode  channel code, unique, immutable
     * @param sortOrder    manual sort order, null falls back to the default
     * @param channelName  channel display name
     * @param providerType provider type (SPI channelCode)
     * @param config       JSON config, optional
     * @param remark       remark, optional
     * @return new channel id
     */
    @Transactional
    public String insert(String operatorId, String channelCode, Integer sortOrder, String channelName,
            String providerType, String config, String remark) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.INSERT);
        if (repository.findByCode(channelCode) != null) {
            throw BusinessException.badRequest("channel code already exists: " + channelCode);
        }

        JulyOutboundChannel channel = newChannel(channelCode, sortOrder, channelName, providerType, config, remark);
        repository.insert(channel);

        return channel.id().value();
    }

    /**
     * Update a channel (channelCode is immutable).
     *
     * @param id           channel id
     * @param channelName  channel display name
     * @param providerType provider type
     * @param config       JSON config
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param status       row status, null keeps the stored one
     * @param remark       remark
     * @return channel id
     */
    @Transactional
    public String update(String operatorId, String id, String channelName, String providerType, String config,
            Integer sortOrder, String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.UPDATE);
        JulyOutboundChannel channel = require(id);
        requireStatus(status);
        String mergedConfig = ChannelConfigCipher011.mergeKeepingSecrets(channel.config(), config);

        try {
            channel.update(channelName, providerType, mergedConfig, sortOrder, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(channel);

        return channel.id().value();
    }

    /**
     * Logic delete a channel.
     *
     * @param id channel id
     * @return deleted channel id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.LOGIC_DELETE);
        require(id);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Batch logic delete, all-or-nothing: a missing id fails the whole batch.
     *
     * @param ids channel ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.LOGIC_DELETE);
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find a channel by primary key.
     *
     * @param id channel id
     * @return aggregate
     */
    public JulyOutboundChannel getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyOutboundChannel> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyOutboundChannelQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundChannelPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyOutboundChannelQuerySpec condition = spec == null ? new JulyOutboundChannelQuerySpec(null, null) : spec;
        List<JulyOutboundChannel> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Build a new aggregate, translating domain validation failures into 400.
     *
     * @param channelCode  channel code
     * @param sortOrder    manual sort order
     * @param channelName  channel display name
     * @param providerType provider type
     * @param config       JSON config
     * @param remark       remark
     * @return new aggregate
     */
    private JulyOutboundChannel newChannel(String channelCode, Integer sortOrder, String channelName, String providerType,
            String config, String remark) {
        try {
            return JulyOutboundChannel.create(EntityId.generate(), channelCode, sortOrder, channelName, providerType,
                    config, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing channel.
     *
     * @param id channel id
     * @return aggregate
     */
    private JulyOutboundChannel require(String id) {
        JulyOutboundChannel channel = repository.findById(id);

        if (channel == null) {
            throw BusinessException.recordNotFound(id);
        }

        return channel;
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

    /**
     * Normalize a batch id list.
     *
     * @param ids raw ids
     * @return normalized ids, never null
     */
    private List<String> normalize(List<String> ids) {
        return ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
    }
}

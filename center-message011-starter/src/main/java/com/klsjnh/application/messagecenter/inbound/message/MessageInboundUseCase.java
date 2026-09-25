package com.klsjnh.application.messagecenter.inbound.message;

/*                MessageInboundUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound use case class
 *      2026.09.26  explicit permission checks (julyMessageInbound)
 *      2026.09.26  require supporting listener; allow anonymous operatorId
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.application.messagecenter.inbound.channel.MessageInboundRegistry;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessage;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessageListener;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundReply;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelRepository;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageInboundPort;
import com.klsjnh.domain.messagecenter.inbound.message.JulyMessageInboundPermissionCodes011;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessage;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageQuerySpec;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageRepository;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MessageInbound use cases: the unified receive path (resolve channel, parse
 * through the inbound registry, dedupe, require at least one supporting
 * listener, persist and dispatch), the received record page query and the
 * record logic delete.
 */

@Service
public class MessageInboundUseCase {

    /**
     * Inbound channel repository.
     */
    private final JulyInboundChannelRepository channelRepository;

    /**
     * Received record repository.
     */
    private final JulyInboundMessageRepository messageRepository;

    /**
     * Inbound port and listener registry.
     */
    private final MessageInboundRegistry registry;

    private final AuthorizationPort authorizationPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageInboundUseCase(JulyInboundChannelRepository channelRepository,
            JulyInboundMessageRepository messageRepository, MessageInboundRegistry registry,
            AuthorizationPort authorizationPort) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.registry = registry;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Receive and handle one inbound message.
     * <p>
     * {@code operatorId} may be null for anonymous vendor HTTP callbacks (after
     * JWT path whitelist) or in-process long-connection plugins; channel-side
     * signature verification belongs in {@link MessageInboundPort#parse}. A
     * non-blank operator still goes through {@code assertHas(RECEIVE)}.
     * </p>
     * <p>
     * At least one {@link InboundMessageListener} must {@code supports} the
     * channel instance code; Port-only persistence is not a completed receive.
     * </p>
     *
     * @param operatorId  operator id, nullable when anonymous
     * @param channelCode channel code
     * @param rawBody     raw request body, nullable
     * @return receive result, never null
     */
    @Transactional
    public MessageInboundResult receive(String operatorId, String channelCode, String rawBody) {
        if (!StringUtil011.isBlank(operatorId)) {
            authorizationPort.assertHas(operatorId, JulyMessageInboundPermissionCodes011.RECEIVE);
        }

        if (StringUtil011.isBlank(channelCode)) {
            throw BusinessException.badRequest("channel code is required");
        }

        JulyInboundChannel channel = channelRepository.findEnabledByCode(channelCode);

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + channelCode);
        }

        MessageInboundPort port = registry.port(channel.providerType());

        if (port == null) {
            throw BusinessException.badRequest("no inbound port for provider type: " + channel.providerType());
        }

        InboundMessage event = port.parse(parseConfig(channel.config()), rawBody);

        if (event == null) {
            throw BusinessException.badRequest("inbound channel returned an empty message");
        }

        if (!StringUtil011.isBlank(event.rawMessageId())) {
            JulyInboundMessage existing = messageRepository.findByChannelAndRawId(channelCode, event.rawMessageId());
            if (existing != null) {
                return new MessageInboundResult(existing.id().value(), true, null);
            }
        }

        List<InboundMessageListener> listeners = registry.listenersFor(channel.channelCode());

        if (listeners.isEmpty()) {
            throw BusinessException.badRequest(
                    "no inbound listener supports channel: " + channel.channelCode()
                            + " (Port alone is not enough; register an InboundMessageListener)");
        }

        JulyInboundMessage message = newMessage(channel, event);
        messageRepository.insert(message);

        InboundReply reply = handle(listeners, event, message);

        messageRepository.update(message);

        String responseBody = reply == null ? null : port.encodeReply(reply);

        return new MessageInboundResult(message.id().value(), false, responseBody);
    }

    /**
     * Page query on the received records.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyInboundMessage> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyInboundMessageQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyMessageInboundPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyInboundMessageQuerySpec condition = spec == null ? new JulyInboundMessageQuerySpec(null, null, null) : spec;
        List<JulyInboundMessage> rows = messageRepository.findPage(query.offset(), query.pageSize(), condition);
        long total = messageRepository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Logic delete a received record.
     *
     * @param id received record id
     * @return deleted record id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageInboundPermissionCodes011.LOGIC_DELETE);

        if (messageRepository.findById(id) == null) {
            throw BusinessException.recordNotFound(id);
        }

        if (!messageRepository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Batch logic delete, all-or-nothing: a missing id fails the whole batch.
     *
     * @param ids received record ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyMessageInboundPermissionCodes011.LOGIC_DELETE);
        List<String> normalized = ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        messageRepository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Dispatch to the supporting listeners (already filtered); the first
     * non-null reply wins. A listener exception marks the record failed and the
     * loop keeps going so overall failure stays observable on the record.
     *
     * @param listeners supporting listeners, never empty at call site
     * @param event     inbound event
     * @param message   persisted record
     * @return first reply, or null when no listener replied
     */
    private InboundReply handle(List<InboundMessageListener> listeners, InboundMessage event,
            JulyInboundMessage message) {
        InboundReply reply = null;

        for (InboundMessageListener listener : listeners) {
            try {
                InboundReply candidate = listener.onMessage(event);

                if (candidate != null) {
                    reply = candidate;
                    break;
                }

                message.markHandled();
            } catch (Exception ex) {
                message.markFailed(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        }

        if (reply != null) {
            message.markHandled();
        }

        return reply;
    }

    /**
     * Build the received record from the channel and event.
     *
     * @param channel inbound channel config
     * @param event   inbound event
     * @return new aggregate
     */
    private JulyInboundMessage newMessage(JulyInboundChannel channel, InboundMessage event) {
        try {
            return JulyInboundMessage.create(EntityId.generate(), channel.channelCode(), channel.providerType(),
                    event.messageType(), toJson(event.payload()), event.fromId(), event.content(),
                    event.rawMessageId(), event.remark(), AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Parse the channel config JSON into a flat key-value map handed to the
     * channel port. Blank or unparseable config yields an empty map.
     *
     * @param config channel config JSON, nullable
     * @return key-value map, never null
     */
    private Map<String, String> parseConfig(String config) {
        if (StringUtil011.isBlank(config)) {
            return Map.of();
        }

        try {
            Map<String, Object> raw = objectMapper.readValue(config, new TypeReference<Map<String, Object>>() {
            });
            Map<String, String> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                result.put(entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
            }

            return result;
        } catch (Exception ex) {
            return Map.of();
        }
    }

    /**
     * Serialize a payload map to JSON for persistence; blank or empty yields
     * null so the column stays empty.
     *
     * @param payload payload map, nullable
     * @return JSON string or null
     */
    private String toJson(Map<String, String> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return null;
        }
    }
}

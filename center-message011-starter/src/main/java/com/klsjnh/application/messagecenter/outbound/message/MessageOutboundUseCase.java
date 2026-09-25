package com.klsjnh.application.messagecenter.outbound.message;

/*                MessageOutboundUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message center use case class
 *      2026.09.26  explicit permission checks (julyMessageOutbound)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.application.messagecenter.outbound.channel.MessageChannelRegistry;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannel;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannelRepository;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageResult;
import com.klsjnh.domain.messagecenter.outbound.message.JulyMessageOutboundPermissionCodes011;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessage;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessageQuerySpec;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessageRepository;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplate;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateRepository;
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
 * MessageCenter use cases: the unified send path (resolve channel, render
 * template, dispatch through the channel registry, persist an outbox row), the
 * send record page query and the failed resend.
 */

@Service
public class MessageOutboundUseCase {

    /**
     * Channel repository.
     */
    private final JulyOutboundChannelRepository channelRepository;

    /**
     * Template repository.
     */
    private final JulyOutboundTemplateRepository templateRepository;

    /**
     * Send record repository.
     */
    private final JulyOutboundMessageRepository messageRepository;

    /**
     * Channel port registry.
     */
    private final MessageChannelRegistry channelRegistry;

    private final AuthorizationPort authorizationPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageOutboundUseCase(JulyOutboundChannelRepository channelRepository,
            JulyOutboundTemplateRepository templateRepository, JulyOutboundMessageRepository messageRepository,
            MessageChannelRegistry channelRegistry, AuthorizationPort authorizationPort) {
        this.channelRepository = channelRepository;
        this.templateRepository = templateRepository;
        this.messageRepository = messageRepository;
        this.channelRegistry = channelRegistry;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Send one message through the resolved channel.
     *
     * @param command send command
     * @return send result, never null
     */
    @Transactional
    public MessageSendResult send(String operatorId, MessageSendCommand command) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundPermissionCodes011.SEND);

        if (command == null || StringUtil011.isBlank(command.channelCode())) {
            throw BusinessException.badRequest("channel code is required");
        }

        JulyOutboundChannel channel = channelRepository.findEnabledByCode(command.channelCode());

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + command.channelCode());
        }

        String title = command.title();
        String content = command.content();

        if (!StringUtil011.isBlank(command.templateCode())) {
            JulyOutboundTemplate template = templateRepository.findEnabledByCode(command.templateCode());
            if (template == null) {
                throw BusinessException.badRequest("template not enabled or not found: " + command.templateCode());
            }
            title = render(StringUtil011.isBlank(title) ? template.title() : title, command.params());
            content = render(StringUtil011.isBlank(content) ? template.content() : content, command.params());
        }

        JulyOutboundMessage message = newMessage(channel, command, title, content);
        MessageResult result = dispatch(channel, command, title, content);
        message.markResult(result);
        messageRepository.insert(message);

        return new MessageSendResult(message.id().value(), result.success(), result.channelMessageId(), result.error());
    }

    /**
     * Page query on the send records.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyOutboundMessage> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyOutboundMessageQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyOutboundMessageQuerySpec condition = spec == null ? new JulyOutboundMessageQuerySpec(null, null, null) : spec;
        List<JulyOutboundMessage> rows = messageRepository.findPage(query.offset(), query.pageSize(), condition);
        long total = messageRepository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Resend a stored message record through its channel.
     *
     * @param id send record id
     * @return send result, never null
     */
    @Transactional
    public MessageSendResult resend(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundPermissionCodes011.RESEND);

        JulyOutboundMessage message = messageRepository.findById(id);

        if (message == null) {
            throw BusinessException.recordNotFound(id);
        }

        JulyOutboundChannel channel = channelRepository.findEnabledByCode(message.channelCode());

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + message.channelCode());
        }

        MessageSendCommand command = new MessageSendCommand(message.channelCode(), message.msgTo(),
                message.messageType(), parseConfig(message.payload()), message.templateCode(), null, message.title(),
                message.content(), message.remark());

        message.countRetry();
        MessageResult result = dispatch(channel, command, message.title(), message.content());
        message.markResult(result);
        messageRepository.update(message);

        return new MessageSendResult(message.id().value(), result.success(), result.channelMessageId(), result.error());
    }

    /**
     * Logic delete a send record.
     *
     * @param id send record id
     * @return deleted record id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundPermissionCodes011.LOGIC_DELETE);

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
     * @param ids send record ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundPermissionCodes011.LOGIC_DELETE);
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
     * Dispatch through the channel registry, translating unknown channels into
     * 400 and channel exceptions into a failure result.
     *
     * @param channel channel config
     * @param command send command
     * @param title   rendered title
     * @param content rendered content
     * @return channel result, never null
     */
    private MessageResult dispatch(JulyOutboundChannel channel, MessageSendCommand command, String title,
            String content) {
        MessageChannelPort port = channelRegistry.get(channel.providerType());

        if (port == null) {
            throw BusinessException.badRequest("no channel port for provider type: " + channel.providerType());
        }

        MessageCommand outbound = new MessageCommand(channel.providerType(), command.to(), command.messageType(),
                parseConfig(channel.config()), command.payload(), command.templateCode(), command.params(), title,
                content);

        try {
            MessageResult result = port.send(outbound);

            return result == null ? MessageResult.failure("empty channel result") : result;
        } catch (Exception ex) {
            return MessageResult.failure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Build the outbox row from the channel and command.
     *
     * @param channel channel config
     * @param command send command
     * @param title   rendered title
     * @param content rendered content
     * @return new aggregate
     */
    private JulyOutboundMessage newMessage(JulyOutboundChannel channel, MessageSendCommand command, String title, String content) {
        try {
            return JulyOutboundMessage.create(EntityId.generate(), channel.channelCode(), channel.providerType(),
                    command.messageType(), toJson(command.payload()), command.to(), command.templateCode(), title,
                    content, command.remark(), AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Render ${var} placeholders from the params map; a null map returns the
     * raw text.
     *
     * @param text   raw text, nullable
     * @param params variables, nullable
     * @return rendered text, nullable when text is null
     */
    private String render(String text, Map<String, String> params) {
        if (text == null || params == null || params.isEmpty()) {
            return text;
        }

        String rendered = text;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() != null) {
                rendered = rendered.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
            }
        }

        return rendered;
    }

    /**
     * Parse the channel config JSON into a flat key-value map handed to the
     * channel port. Blank or unparseable config yields an empty map; the config
     * is platform-authored, not user text.
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

package com.klsjnh.application.messagecenter.message;

/*                MessageCenterUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message center use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.application.messagecenter.channel.MessageChannelRegistry;
import com.klsjnh.domain.messagecenter.channel.JulyMessageChannel;
import com.klsjnh.domain.messagecenter.channel.JulyMessageChannelRepository;
import com.klsjnh.domain.messagecenter.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.channel.MessageResult;
import com.klsjnh.domain.messagecenter.message.JulyMessage;
import com.klsjnh.domain.messagecenter.message.JulyMessageQuerySpec;
import com.klsjnh.domain.messagecenter.message.JulyMessageRepository;
import com.klsjnh.domain.messagecenter.template.JulyMessageTemplate;
import com.klsjnh.domain.messagecenter.template.JulyMessageTemplateRepository;
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
public class MessageCenterUseCase {

    /**
     * Channel repository.
     */
    private final JulyMessageChannelRepository channelRepository;

    /**
     * Template repository.
     */
    private final JulyMessageTemplateRepository templateRepository;

    /**
     * Send record repository.
     */
    private final JulyMessageRepository messageRepository;

    /**
     * Channel port registry.
     */
    private final MessageChannelRegistry channelRegistry;

    /**
     * JSON mapper for the channel config.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the use case.
     *
     * @param channelRepository  channel repository
     * @param templateRepository template repository
     * @param messageRepository  send record repository
     * @param channelRegistry    channel port registry
     */
    public MessageCenterUseCase(JulyMessageChannelRepository channelRepository,
            JulyMessageTemplateRepository templateRepository, JulyMessageRepository messageRepository,
            MessageChannelRegistry channelRegistry) {
        this.channelRepository = channelRepository;
        this.templateRepository = templateRepository;
        this.messageRepository = messageRepository;
        this.channelRegistry = channelRegistry;
    }

    /**
     * Send one message through the resolved channel.
     *
     * @param command send command
     * @return send result, never null
     */
    @Transactional
    public MessageSendResult send(MessageSendCommand command) {
        if (command == null || StringUtil011.isBlank(command.channelCode())) {
            throw BusinessException.badRequest("channel code is required");
        }

        JulyMessageChannel channel = channelRepository.findEnabledByCode(command.channelCode());

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + command.channelCode());
        }

        String title = command.title();
        String content = command.content();

        if (!StringUtil011.isBlank(command.templateCode())) {
            JulyMessageTemplate template = templateRepository.findEnabledByCode(command.templateCode());
            if (template == null) {
                throw BusinessException.badRequest("template not enabled or not found: " + command.templateCode());
            }
            title = render(StringUtil011.isBlank(title) ? template.title() : title, command.params());
            content = render(StringUtil011.isBlank(content) ? template.content() : content, command.params());
        }

        JulyMessage message = newMessage(channel, command, title, content);
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
    public PageResult011<JulyMessage> selectListByPage(PageQuery011 pageQuery, JulyMessageQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyMessageQuerySpec condition = spec == null ? new JulyMessageQuerySpec(null, null, null) : spec;
        List<JulyMessage> rows = messageRepository.findPage(query.offset(), query.pageSize(), condition);
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
    public MessageSendResult resend(String id) {
        JulyMessage message = messageRepository.findById(id);

        if (message == null) {
            throw BusinessException.recordNotFound(id);
        }

        JulyMessageChannel channel = channelRepository.findEnabledByCode(message.channelCode());

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + message.channelCode());
        }

        MessageSendCommand command = new MessageSendCommand(message.channelCode(), message.msgTo(),
                message.templateCode(), null, message.title(), message.content(), message.remark());

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
    public String logicDelete(String id) {
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
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
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
    private MessageResult dispatch(JulyMessageChannel channel, MessageSendCommand command, String title,
            String content) {
        MessageChannelPort port = channelRegistry.get(channel.providerType());

        if (port == null) {
            throw BusinessException.badRequest("no channel port for provider type: " + channel.providerType());
        }

        MessageCommand outbound = new MessageCommand(channel.providerType(), command.to(),
                parseConfig(channel.config()), command.templateCode(), command.params(), title, content);

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
    private JulyMessage newMessage(JulyMessageChannel channel, MessageSendCommand command, String title, String content) {
        try {
            return JulyMessage.create(EntityId.generate(), null, channel.channelCode(), channel.providerType(),
                    command.to(), command.templateCode(), title, content, command.remark(), AuditInfo.empty());
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
}

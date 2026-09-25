package com.klsjnh.application.messagecenter.inbound.message;

/*                MessageInboundUseCaseReceiveTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  require supporting listener; anonymous operatorId
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.messagecenter.inbound.channel.MessageInboundRegistry;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessage;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessageListener;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundReply;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelRepository;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageInboundPort;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

/**
 * Receive-path unit tests: zero supporting listeners fail; anonymous operatorId
 * skips assertHas.
 */

@ExtendWith(MockitoExtension.class)
class MessageInboundUseCaseReceiveTest {

    @Mock
    private JulyInboundChannelRepository channelRepository;

    @Mock
    private JulyInboundMessageRepository messageRepository;

    @Mock
    private AuthorizationPort authorizationPort;

    @Mock
    private MessageInboundPort inboundPort;

    /**
     * Zero listeners that support the channel -> badRequest, no insert.
     */
    @Test
    void receiveFailsWhenNoListenerSupportsChannel() {
        when(inboundPort.channelCode()).thenReturn("feishu");
        when(channelRepository.findEnabledByCode("feishu_demo")).thenReturn(sampleChannel());
        when(inboundPort.parse(any(), anyString())).thenReturn(sampleEvent());

        MessageInboundRegistry registry = new MessageInboundRegistry(List.of(inboundPort), List.of());
        MessageInboundUseCase useCase = new MessageInboundUseCase(channelRepository, messageRepository, registry,
                authorizationPort);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.receive(null, "feishu_demo", "{\"x\":1}"));

        assertTrue(ex.getMessage().contains("no inbound listener supports channel"));
        verify(messageRepository, never()).insert(any());
        verify(authorizationPort, never()).assertHas(any(), anyString());
    }

    /**
     * At least one supporting listener -> receive proceeds (anonymous operator).
     */
    @Test
    void receiveSucceedsWithSupportingListenerAndAnonymousOperator() {
        when(inboundPort.channelCode()).thenReturn("feishu");
        when(channelRepository.findEnabledByCode("feishu_demo")).thenReturn(sampleChannel());
        when(inboundPort.parse(any(), anyString())).thenReturn(sampleEvent());

        InboundMessageListener listener = new InboundMessageListener() {
            @Override
            public boolean supports(String channelCode) {
                return channelCode != null && channelCode.startsWith("feishu");
            }

            /** {@inheritDoc} */
            @Override
            public InboundReply onMessage(InboundMessage message) {
                return null;
            }
        };

        MessageInboundRegistry registry = new MessageInboundRegistry(List.of(inboundPort), List.of(listener));
        MessageInboundUseCase useCase = new MessageInboundUseCase(channelRepository, messageRepository, registry,
                authorizationPort);

        assertDoesNotThrow(() -> useCase.receive(null, "feishu_demo", "{\"x\":1}"));
        verify(messageRepository).insert(any());
        verify(authorizationPort, never()).assertHas(isNull(), anyString());
        verify(authorizationPort, never()).assertHas(eq(""), anyString());
    }

    /**
     * Authenticated receive still calls assertHas.
     */
    @Test
    void receiveWithOperatorAssertsPermission() {
        when(inboundPort.channelCode()).thenReturn("feishu");
        when(channelRepository.findEnabledByCode("feishu_demo")).thenReturn(sampleChannel());
        when(inboundPort.parse(any(), anyString())).thenReturn(sampleEvent());

        InboundMessageListener listener = new InboundMessageListener() {
            @Override
            public boolean supports(String channelCode) {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public InboundReply onMessage(InboundMessage message) {
                return null;
            }
        };

        MessageInboundRegistry registry = new MessageInboundRegistry(List.of(inboundPort), List.of(listener));
        MessageInboundUseCase useCase = new MessageInboundUseCase(channelRepository, messageRepository, registry,
                authorizationPort);

        assertDoesNotThrow(() -> useCase.receive("op-1", "feishu_demo", "{}"));
        verify(authorizationPort).assertHas(eq("op-1"), anyString());
    }

    /**
     * Builds a sample enabled inbound channel for tests.
     *
     * @return sample channel
     */
    private static JulyInboundChannel sampleChannel() {
        return JulyInboundChannel.create(EntityId.generate(), "feishu_demo", null, "feishu demo", "feishu", "{}",
                null, AuditInfo.empty());
    }

    /**
     * Builds a sample parsed inbound message for tests.
     *
     * @return sample event
     */
    private static InboundMessage sampleEvent() {
        return new InboundMessage("feishu_demo", "feishu", "u1", "text", Map.of(), "hi", "raw-1", null);
    }
}
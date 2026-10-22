package com.klsjnh.application.messagecenter.inbound.message;

/*                MessageMediaServiceTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  media port resolution tests
 *
 */

import com.klsjnh.application.messagecenter.inbound.channel.MessageMediaRegistry;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelRepository;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageMedia;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageMediaPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Unit tests for {@link MessageMediaService}: channel to provider resolution.
 */

class MessageMediaServiceTest {

    /**
     * Resolves the channel provider and delegates the download.
     */
    @Test
    void downloadsThroughProviderPort() {
        JulyInboundChannelRepository repository = mock(JulyInboundChannelRepository.class);
        MessageMediaPort port = mock(MessageMediaPort.class);
        when(port.channelCode()).thenReturn("feishu");
        when(repository.findEnabledByCode("feishu_demo"))
                .thenReturn(JulyInboundChannel.create(EntityId.generate(), "feishu_demo", null, "feishu demo",
                        "feishu", "{}", null, AuditInfo.empty()));
        when(port.download("feishu_demo", "om_1", "file_1", "audio"))
                .thenReturn(new MessageMedia("voice.opus", "audio/ogg", "opus".getBytes(StandardCharsets.UTF_8)));

        MessageMediaService service = new MessageMediaService(repository, new MessageMediaRegistry(List.of(port)));

        MessageMedia media = service.download("feishu_demo", "om_1", "file_1", "audio");

        assertEquals("voice.opus", media.fileName());
        assertEquals("audio/ogg", media.contentType());
    }
}

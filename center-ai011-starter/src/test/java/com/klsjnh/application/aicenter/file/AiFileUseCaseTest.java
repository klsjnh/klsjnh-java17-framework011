package com.klsjnh.application.aicenter.file;

/*                AiFileUseCaseTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  file use case tests
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.file.AiFilePort;
import com.klsjnh.domain.aicenter.file.AiFilePurpose;
import com.klsjnh.domain.aicenter.file.AiFileRef;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

/**
 * Unit tests for {@link AiFileUseCase}.
 */

class AiFileUseCaseTest {

    private AiFilePort port;
    private AiFileUseCase useCase;
    private AiInvokeTarget target;

    @BeforeEach
    /**
     * Creates the use case with mocked resolver / registry / port.
     */
    void setUp() {
        AiProviderResolver resolver = mock(AiProviderResolver.class);
        AiCapabilityRegistry registry = mock(AiCapabilityRegistry.class);
        AuthorizationPort authorizationPort = mock(AuthorizationPort.class);
        port = mock(AiFilePort.class);

        AiModelProvider provider = mock(AiModelProvider.class);
        when(provider.providerCode()).thenReturn("stepfun");
        when(provider.baseUrl()).thenReturn("https://api.stepfun.com/v1");

        AiModelProviderApi api = mock(AiModelProviderApi.class);
        when(api.apiKey()).thenReturn("sk");

        when(resolver.resolveProvider(any(), any())).thenReturn(provider);
        when(resolver.resolveApi(any(), any(), any())).thenReturn(api);
        when(registry.filePort("stepfun")).thenReturn(port);
        when(port.upload(any())).thenReturn(new AiFileRef("stepfun", "file_1", "a.pdf", 3, "file-extract",
                "processed", 1L));

        useCase = new AiFileUseCase(resolver, registry, authorizationPort, new KrtAiConfig011());
        target = new AiInvokeTarget("stepfun", null, null, null, null, null);
    }

    @Test
    /**
     * Uploads bytes through the file port.
     */
    void uploadsBytes() {
        AiFileRef ref = useCase.upload("u1", target, "a.pdf", "pdf".getBytes(StandardCharsets.UTF_8), null,
                AiFilePurpose.TEXT_EXTRACT);

        assertEquals("file_1", ref.fileId());
        verify(port).upload(any());
    }

    @Test
    /**
     * Rejects a file over the configured limit.
     */
    void rejectsOversizedFile() {
        byte[] bytes = new byte[(int) new KrtAiConfig011().getFile().getMaxUploadBytes() + 1];

        assertThrows(BusinessException.class, () -> useCase.upload("u1", target, "a.bin", bytes, null,
                AiFilePurpose.STORAGE));
    }

    @Test
    /**
     * Gets metadata and deletes through the port.
     */
    void getsAndDeletes() {
        when(port.get(any())).thenReturn(new AiFileRef("stepfun", "file_1", "a.pdf", 3, "file-extract",
                "processed", 1L));

        assertEquals("file_1", useCase.get("u1", target, "file_1").fileId());
        useCase.delete("u1", target, "file_1");
        verify(port).delete(any());
    }

    @Test
    /**
     * Lists files and downloads content.
     */
    void listsAndDownloads() {
        when(port.list(any())).thenReturn(java.util.List.of());
        when(port.content(any())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));

        assertEquals(0, useCase.list("u1", target, null).size());
        assertEquals("pdf", new String(useCase.content("u1", target, "file_1"), StandardCharsets.UTF_8));
    }
}

package com.klsjnh.application.platform011.export;

/*                ExportUseCaseStreamTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  stream() use case unit test (permission + audit + batched
 *                  source wiring)
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.user.UserAuditPort;
import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.platform011.export.ExportStreamPort;
import com.klsjnh.domain.platform011.export.SheetRowSource;
import com.klsjnh.domain.platform011.export.XlsxWorkbookPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ExportUseCase#stream}: the per-object export code is
 * asserted, the streaming port pulls the provider's rows through a batched
 * source, and the EXPORT audit lands after the stream completes.
 */

@ExtendWith(MockitoExtension.class)
class ExportUseCaseStreamTest {

    /**
     * Mocked registry.
     */
    @Mock
    private ExportProviderRegistry registry;

    /**
     * Mocked xlsx port (byte[] path, unused here).
     */
    @Mock
    private XlsxWorkbookPort xlsxWorkbookPort;

    /**
     * Mocked streaming port.
     */
    @Mock
    private ExportStreamPort exportStreamPort;

    /**
     * Mocked audit port.
     */
    @Mock
    private UserAuditPort userAuditPort;

    /**
     * Mocked authorization port.
     */
    @Mock
    private AuthorizationPort authorizationPort;

    /**
     * Mocked provider.
     */
    @Mock
    private ExportProvider provider;

    /**
     * Use case under test.
     */
    private ExportUseCase useCase;

    /**
     * Wire the use case with the mocks.
     */
    @BeforeEach
    void setUp() {
        useCase = new ExportUseCase(registry, xlsxWorkbookPort, exportStreamPort, userAuditPort, authorizationPort);
    }

    /**
     * Stream asserts the per-object export code and audits after writing.
     */
    @Test
    void streamAssertsPermissionAndAuditsAfterWrite() {
        when(registry.get("julyConfig")).thenReturn(provider);
        when(provider.moduleCode()).thenReturn("system011");
        when(provider.objectCode()).thenReturn("julyConfig");
        when(provider.columns()).thenReturn(List.of(new ExportColumn("id", "ID")));
        when(provider.sheetSpecs()).thenReturn(List.of(new com.klsjnh.domain.platform011.export.ExportSheetSpec(
                "master", List.of(new ExportColumn("id", "ID")))));
        when(provider.exportRows(anyInt(), anyInt())).thenReturn(List.of(Map.of("id", 1), Map.of("id", 2)));
        when(exportStreamPort.writeCsv(contains("master"), any(), any(SheetRowSource.class), eq(500),
                any(OutputStream.class))).thenAnswer(invocation -> {
                    SheetRowSource source = invocation.getArgument(2);
                    return (long) source.fetch(0, 500).size();
                });

        OutputStream sink = new java.io.ByteArrayOutputStream();
        useCase.stream("csv", "julyConfig", new Operator011("op-1", "admin", "127.0.0.1")).accept(sink);

        verify(authorizationPort).assertHas("op-1", "system011:julyConfig:export");
        verify(userAuditPort).record(eq("op-1"), eq("admin"), eq(AuditType011.EXPORT), eq("julyConfig"),
                contains("export csv stream 2 rows"), eq("127.0.0.1"));
        verify(provider).exportRows(0, 500);
    }

    /**
     * An unknown object is rejected with 400.
     */
    @Test
    void unknownObjectIsRejected() {
        when(registry.get("nope")).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> useCase.stream("csv", "nope", new Operator011("op-1", "admin", "127.0.0.1")));
    }

    /**
     * Providers listing requires authentication and reports module + columns.
     */
    @Test
    void providersRequireAuthentication() {
        assertThrows(BusinessException.class, () -> useCase.providers(null));

        when(registry.all()).thenReturn(List.of(provider));
        when(provider.objectCode()).thenReturn("julyConfig");
        when(provider.moduleCode()).thenReturn("system011");
        when(provider.columns()).thenReturn(List.of(new ExportColumn("id", "ID")));

        assertEquals(1, useCase.providers(new Operator011("op-1", "admin", "127.0.0.1")).size());
    }
}

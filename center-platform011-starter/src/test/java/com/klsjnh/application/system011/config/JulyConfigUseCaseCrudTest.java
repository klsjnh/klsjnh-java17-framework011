package com.klsjnh.application.system011.config;

/*                JulyConfigUseCaseCrudTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  config insert / getById unit test
 *
 */

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigRepository;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for insert + getById on {@link JulyConfigUseCase}.
 */

@ExtendWith(MockitoExtension.class)
class JulyConfigUseCaseCrudTest {

    @Mock
    private JulyConfigRepository repository;
    @Mock
    private AuthorizationPort authorizationPort;
    @Mock
    private ExportUseCase exportUseCase;
    @Mock
    private BackupUseCase backupUseCase;

    private JulyConfigUseCase useCase;

    /**
     * Wire the use case with mocked ports.
     */
    @BeforeEach
    void setUp() {
        useCase = new JulyConfigUseCase(repository, authorizationPort, exportUseCase, backupUseCase);
    }

    /**
     * Insert persists a row that getById can load.
     */
    @Test
    void insertThenGetById() {
        doNothing().when(authorizationPort).assertHas(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        when(repository.findEnabledByCode("demo.code")).thenReturn(null);

        String id = useCase.insert("op1", "demo.code", "v1", "1", "remark");

        assertNotNull(id);
        ArgumentCaptor<JulyConfig> captor = ArgumentCaptor.forClass(JulyConfig.class);
        verify(repository).insert(captor.capture());
        JulyConfig saved = captor.getValue();
        assertEquals("demo.code", saved.code());
        assertEquals("v1", saved.data());

        when(repository.findById(id)).thenReturn(saved);
        JulyConfig loaded = useCase.getById("op1", id);
        assertEquals("demo.code", loaded.code());
        assertEquals(id, loaded.id().value());
    }

    /**
     * Update changes the stored data field.
     */
    @Test
    void updateChangesData() {
        doNothing().when(authorizationPort).assertHas(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        JulyConfig existing = JulyConfig.create(EntityId.of("c1"), "demo.code", "old", "1", null, AuditInfo.empty());
        when(repository.findById("c1")).thenReturn(existing);

        String id = useCase.update("op1", "c1", "new", "1", "r");

        assertEquals("c1", id);
        verify(repository).update(existing);
        assertEquals("new", existing.data());
    }
}

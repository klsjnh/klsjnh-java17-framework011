package com.klsjnh.infrastructure.iam.auth;

/*                AuthorizationAdapterTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  unit test for authorization adapter
 *      2026.09.26  production-only checks; debug no-op
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.auth.PermissionCheckContext011;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;
import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.iam.user.JulyUserPermissionCodes011;
import com.klsjnh.domain.iam.role.JulyRolePermissionsRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for {@link AuthorizationAdapter} covering role shapes (builtin
 * bypass, backup-only, select-only) and debug no-op, using julyUser codes as
 * sample permission strings (platform julyConfig codes live in the platform
 * starter).
 */

@ExtendWith(MockitoExtension.class)
class AuthorizationAdapterTest {

    @Mock
    private JulyUserRoleRepository userRoleRepository;

    @Mock
    private JulyRoleRepository roleRepository;

    @Mock
    private JulyRolePermissionsRepository rolePermissionsRepository;

    @Mock
    private RuntimeStatusPort runtimeStatusPort;

    private AuthorizationAdapter adapter;

    /**
     * Wire a fresh adapter with the mocked repositories before each test.
     */
    @BeforeEach
    void setUp() {
        PermissionCheckContext011.clear();
        adapter = new AuthorizationAdapter(userRoleRepository, roleRepository, rolePermissionsRepository,
                runtimeStatusPort);
    }

    /**
     * Clear the per-request mark after each test.
     */
    @AfterEach
    void tearDown() {
        PermissionCheckContext011.clear();
    }

    /**
     * Built-in roles bypass every permission code check.
     */
    @Test
    void builtinRoleBypassesAllCodes() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(true);
        when(userRoleRepository.findRoleIds("u-super")).thenReturn(List.of("r-builtin"));
        when(roleRepository.findById("r-builtin")).thenReturn(
                JulyRole.createBuiltin(EntityId.of("r-builtin"), "role_super_admin", "超管", null, AuditInfo.empty()));

        assertTrue(adapter.has("u-super", JulyUserPermissionCodes011.SELECT));
        assertTrue(adapter.has("u-super", JulyUserPermissionCodes011.BACKUP));
        assertDoesNotThrow(() -> adapter.assertHas("u-super", JulyUserPermissionCodes011.LOGIC_DELETE));
    }

    /**
     * A backup-only role may backup but cannot select.
     */
    @Test
    void backupOnlyCannotSelect() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(true);
        when(userRoleRepository.findRoleIds("u-backup")).thenReturn(List.of("r-backup"));
        when(roleRepository.findById("r-backup")).thenReturn(
                JulyRole.create(EntityId.of("r-backup"), "role_backup_admin", "备份", null, AuditInfo.empty()));
        when(rolePermissionsRepository.findPermissionCodes("r-backup"))
                .thenReturn(List.of(JulyUserPermissionCodes011.BACKUP));

        assertTrue(adapter.has("u-backup", JulyUserPermissionCodes011.BACKUP));
        assertFalse(adapter.has("u-backup", JulyUserPermissionCodes011.SELECT));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adapter.assertHas("u-backup", JulyUserPermissionCodes011.SELECT));
        assertEquals(403, ex.getCode());
    }

    /**
     * A select-only role may select but cannot logic-delete.
     */
    @Test
    void selectOnlyCannotDelete() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(true);
        when(userRoleRepository.findRoleIds("u-user")).thenReturn(List.of("r-user"));
        when(roleRepository.findById("r-user")).thenReturn(
                JulyRole.create(EntityId.of("r-user"), "role_user", "普通", null, AuditInfo.empty()));
        when(rolePermissionsRepository.findPermissionCodes("r-user"))
                .thenReturn(List.of(JulyUserPermissionCodes011.SELECT));

        assertTrue(adapter.has("u-user", JulyUserPermissionCodes011.SELECT));
        assertFalse(adapter.has("u-user", JulyUserPermissionCodes011.LOGIC_DELETE));
    }

    /**
     * A blank operator id is rejected as unauthorized (401) in production.
     */
    @Test
    void blankOperatorUnauthorized() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adapter.assertHas("", JulyUserPermissionCodes011.SELECT));
        assertEquals(401, ex.getCode());
    }

    /**
     * assertHas marks the request so the production whitelist gate can see it.
     */
    @Test
    void assertHasMarksPermissionCheckContext() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(true);
        assertFalse(PermissionCheckContext011.wasChecked());
        when(userRoleRepository.findRoleIds("u-super")).thenReturn(List.of("r-builtin"));
        when(roleRepository.findById("r-builtin")).thenReturn(
                JulyRole.createBuiltin(EntityId.of("r-builtin"), "role_super_admin", "超管", null, AuditInfo.empty()));

        assertDoesNotThrow(() -> adapter.assertHas("u-super", JulyUserPermissionCodes011.SELECT));
        assertTrue(PermissionCheckContext011.wasChecked());
    }

    /**
     * Debug / development: assertHas is a no-op (marks only, no catalog lookup).
     */
    @Test
    void debugAssertHasIsNoOp() {
        when(runtimeStatusPort.isPermissionWhitelistMode()).thenReturn(false);

        assertDoesNotThrow(() -> adapter.assertHas("", JulyUserPermissionCodes011.SELECT));
        assertTrue(adapter.has("anyone", JulyUserPermissionCodes011.LOGIC_DELETE));
        assertTrue(PermissionCheckContext011.wasChecked());
        verify(userRoleRepository, never()).findRoleIds(org.mockito.ArgumentMatchers.any());
    }
}

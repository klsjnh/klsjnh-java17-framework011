package com.klsjnh.infrastructure.iam.auth;

/*                AuthorizationAdapterTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  unit test for authorization adapter
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfigPermissionCodes011;
import com.klsjnh.domain.iam.role.JulyRolePermissionsRepository;

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
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for {@link AuthorizationAdapter} covering the julyConfig demo
 * role shapes (builtin bypass, backup-only, select-only).
 */

@ExtendWith(MockitoExtension.class)
class AuthorizationAdapterTest {

    @Mock
    private JulyUserRoleRepository userRoleRepository;

    @Mock
    private JulyRoleRepository roleRepository;

    @Mock
    private JulyRolePermissionsRepository rolePermissionsRepository;

    private AuthorizationAdapter adapter;

    /**
     * Wire a fresh adapter with the mocked repositories before each test.
     */
    @BeforeEach
    void setUp() {
        adapter = new AuthorizationAdapter(userRoleRepository, roleRepository, rolePermissionsRepository);
    }

    /**
     * Built-in roles bypass every permission code check.
     */
    @Test
    void builtinRoleBypassesAllCodes() {
        when(userRoleRepository.findRoleIds("u-super")).thenReturn(List.of("r-builtin"));
        when(roleRepository.findById("r-builtin")).thenReturn(
                JulyRole.createBuiltin(EntityId.of("r-builtin"), "role_super_admin", "超管", null, AuditInfo.empty()));

        assertTrue(adapter.has("u-super", JulyConfigPermissionCodes011.SELECT));
        assertTrue(adapter.has("u-super", JulyConfigPermissionCodes011.BACKUP));
        assertDoesNotThrow(() -> adapter.assertHas("u-super", JulyConfigPermissionCodes011.LOGIC_DELETE));
    }

    /**
     * A backup-only role may backup but cannot select.
     */
    @Test
    void backupOnlyCannotSelect() {
        when(userRoleRepository.findRoleIds("u-backup")).thenReturn(List.of("r-backup"));
        when(roleRepository.findById("r-backup")).thenReturn(
                JulyRole.create(EntityId.of("r-backup"), "role_backup_admin", "备份", null, AuditInfo.empty()));
        when(rolePermissionsRepository.findPermissionCodes("r-backup"))
                .thenReturn(List.of(JulyConfigPermissionCodes011.BACKUP));

        assertTrue(adapter.has("u-backup", JulyConfigPermissionCodes011.BACKUP));
        assertFalse(adapter.has("u-backup", JulyConfigPermissionCodes011.SELECT));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adapter.assertHas("u-backup", JulyConfigPermissionCodes011.SELECT));
        assertEquals(403, ex.getCode());
    }

    /**
     * A select-only role may select but cannot logic-delete.
     */
    @Test
    void selectOnlyCannotDelete() {
        when(userRoleRepository.findRoleIds("u-user")).thenReturn(List.of("r-user"));
        when(roleRepository.findById("r-user")).thenReturn(
                JulyRole.create(EntityId.of("r-user"), "role_user", "普通", null, AuditInfo.empty()));
        when(rolePermissionsRepository.findPermissionCodes("r-user"))
                .thenReturn(List.of(JulyConfigPermissionCodes011.SELECT));

        assertTrue(adapter.has("u-user", JulyConfigPermissionCodes011.SELECT));
        assertFalse(adapter.has("u-user", JulyConfigPermissionCodes011.LOGIC_DELETE));
    }

    /**
     * A blank operator id is rejected as unauthorized (401).
     */
    @Test
    void blankOperatorUnauthorized() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adapter.assertHas("", JulyConfigPermissionCodes011.SELECT));
        assertEquals(401, ex.getCode());
    }
}

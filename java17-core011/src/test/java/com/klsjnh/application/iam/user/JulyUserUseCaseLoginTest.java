package com.klsjnh.application.iam.user;

/*                JulyUserUseCaseLoginTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  login path unit test
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.PasswordPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;
import com.klsjnh.domain.iam.auth.UserRoleCodesPort;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.iam.user.UserAuditPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for the password login path on {@link JulyUserUseCase}.
 */

@ExtendWith(MockitoExtension.class)
class JulyUserUseCaseLoginTest {

    @Mock
    private JulyUserRepository repository;
    @Mock
    private PasswordPort passwordPort;
    @Mock
    private AuthTokenPort authTokenPort;
    @Mock
    private RuntimeStatusPort runtimeStatusPort;
    @Mock
    private UserRoleCodesPort userRoleCodesPort;
    @Mock
    private UserAuditPort userAuditPort;
    @Mock
    private AuthorizationPort authorizationPort;
    @Mock
    private ExportUseCase exportUseCase;
    @Mock
    private BackupUseCase backupUseCase;

    private JulyUserUseCase useCase;

    /**
     * Wire the use case with mocked ports.
     */
    @BeforeEach
    void setUp() {
        useCase = new JulyUserUseCase(repository, passwordPort, authTokenPort, runtimeStatusPort, userRoleCodesPort,
                userAuditPort, authorizationPort, exportUseCase, backupUseCase);
    }

    /**
     * Successful login issues a token and touches last-login time.
     */
    @Test
    void loginSuccessIssuesToken() {
        JulyUser user = JulyUser.create(EntityId.of("u1"), "demo", "Demo", "hash", AuditInfo.empty());
        when(repository.findByAccount("demo")).thenReturn(user);
        when(passwordPort.matches("secret", "hash")).thenReturn(true);
        when(authTokenPort.issue("u1", "demo", 0)).thenReturn("token-011");
        when(userRoleCodesPort.findRoleCodes("u1")).thenReturn(List.of("admin"));

        LoginResult result = useCase.login("demo", "secret", "127.0.0.1");

        assertEquals("token-011", result.token());
        assertEquals("demo", result.userAccount());
        verify(repository).touchLastLoginTime("u1");
    }

    /**
     * Wrong password raises a business exception.
     */
    @Test
    void loginWrongPasswordFails() {
        JulyUser user = JulyUser.create(EntityId.of("u1"), "demo", "Demo", "hash", AuditInfo.empty());
        when(repository.findByAccount("demo")).thenReturn(user);
        when(passwordPort.matches(anyString(), eq("hash"))).thenReturn(false);

        assertThrows(BusinessException.class, () -> useCase.login("demo", "bad", "127.0.0.1"));
    }

    /**
     * Disabled accounts are rejected after password match.
     */
    @Test
    void loginDisabledAccountFails() {
        JulyUser user = new JulyUser(EntityId.of("u1"), "demo", "Demo", "hash", null, null, null, null, null, 0,
                Status011.DISABLED.getCode(), AuditInfo.empty());
        when(repository.findByAccount("demo")).thenReturn(user);
        when(passwordPort.matches("secret", "hash")).thenReturn(true);

        assertThrows(BusinessException.class, () -> useCase.login("demo", "secret", "127.0.0.1"));
    }

    /**
     * Passwordless login also rejects disabled accounts.
     */
    @Test
    void loginByUserNameDisabledAccountFails() {
        when(runtimeStatusPort.allowsPasswordlessLogin()).thenReturn(true);
        JulyUser user = new JulyUser(EntityId.of("u1"), "demo", "Demo", "hash", null, null, null, null, null, 0,
                Status011.DISABLED.getCode(), AuditInfo.empty());
        when(repository.findByAccount("demo")).thenReturn(user);

        assertThrows(BusinessException.class, () -> useCase.loginByUserName("demo", "127.0.0.1"));
    }
}

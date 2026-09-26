package com.klsjnh.application.iam.user;

/*                JulyUserUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user use case class
 *      2026.09.26  explicit permission checks (julyUser auth)
 *      2026.09.26  tokenVersion revoke on status/password/logout
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.UserRoleCodesPort;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserPermissionCodes011;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.iam.auth.PasswordPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;
import com.klsjnh.domain.iam.user.UserAuditPort;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyUser use cases: user CRUD, password reset and the two login kinds
 * (account+password / passwordless by account, gated by krt.status). Role
 * assignment lives in the access center starter. Management actions assert
 * permission codes via {@link AuthorizationPort}.
 */

@Service
public class JulyUserUseCase {

    /**
     * JulyUser repository.
     */
    private final JulyUserRepository repository;

    /**
     * Password hashing port.
     */
    private final PasswordPort passwordPort;

    /**
     * Auth token port.
     */
    private final AuthTokenPort authTokenPort;

    /**
     * Runtime status port.
     */
    private final RuntimeStatusPort runtimeStatusPort;

    /**
     * Optional role-code lookup (access center; empty list without it).
     */
    private final UserRoleCodesPort userRoleCodesPort;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Platform export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Platform backup use case.
     */
    private final BackupUseCase backupUseCase;

    /**
     * Create the use case.
     *
     * @param repository         july user repository
     * @param passwordPort       password hashing port
     * @param authTokenPort      auth token port
     * @param runtimeStatusPort  runtime status port
     * @param userRoleCodesPort  role codes for login payload
     * @param userAuditPort      user audit port
     * @param authorizationPort  authorization port
     * @param exportUseCase      export use case
     * @param backupUseCase      backup use case
     */
    public JulyUserUseCase(JulyUserRepository repository, PasswordPort passwordPort, AuthTokenPort authTokenPort,
            RuntimeStatusPort runtimeStatusPort, UserRoleCodesPort userRoleCodesPort, UserAuditPort userAuditPort,
            AuthorizationPort authorizationPort, ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.repository = repository;
        this.passwordPort = passwordPort;
        this.authTokenPort = authTokenPort;
        this.runtimeStatusPort = runtimeStatusPort;
        this.userRoleCodesPort = userRoleCodesPort;
        this.userAuditPort = userAuditPort;
        this.authorizationPort = authorizationPort;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new user (password is hashed with bcrypt).
     *
     * @param operatorId  operator user id
     * @param userAccount login account, unique
     * @param userName    user name
     * @param password    raw password
     * @param mobile      mobile number, nullable
     * @param email       email, nullable
     * @return new user id
     */
    @Transactional
    public String insert(String operatorId, String userAccount, String userName, String password, String mobile,
            String email, String avatar, String pkOrg) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.INSERT);

        if (password == null || password.isBlank()) {
            throw BusinessException.badRequest("insert: password is required");
        }

        if (repository.findByAccount(userAccount) != null) {
            throw BusinessException.badRequest("user account already exists: " + userAccount);
        }

        JulyUser user = new JulyUser(EntityId.generate(), userAccount, userName, passwordPort.encode(password),
                mobile, email, avatar, pkOrg, null, 0, Status011.ENABLED.getCode(), AuditInfo.empty());
        repository.insert(user);

        return user.id().value();
    }

    /**
     * Update the profile (account and password are not part of profile
     * updates). Status is optional: a null / blank value leaves the current
     * status untouched; a real change bumps {@code tokenVersion} so prior
     * JWTs fail verify.
     *
     * @param operatorId operator user id
     * @param id         user id
     * @param userName   user name
     * @param mobile     mobile number
     * @param email      email
     * @param avatar     avatar
     * @param pkOrg      organization link
     * @param status     account status ("1" / "0"), nullable
     */
    @Transactional
    public void update(String operatorId, String id, String userName, String mobile, String email, String avatar,
            String pkOrg, String status) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.UPDATE);
        JulyUser user = require(id);
        user.updateProfile(userName, mobile, email, avatar, pkOrg);

        if (status != null && !status.isBlank()) {
            user.changeStatus(status);
        }

        repository.update(user);
    }

    /**
     * Logic delete a single user.
     *
     * @param operatorId operator user id
     * @param id         user id
     * @return deleted user id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.LOGIC_DELETE);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Logic delete users, all-or-nothing: a missing id fails the whole batch
     * (404) so the transaction rolls back.
     *
     * <p>Note: the user_role junction rows are NOT cascaded here — role
     * assignment is a separate aggregate and the relation is inactive as soon
     * as the user is deleted.</p>
     *
     * @param operatorId operator user id
     * @param ids        user ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.LOGIC_DELETE);
        List<String> normalized = normalizeIds(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Normalize an id list: drop null / blank entries, trim and de-duplicate.
     *
     * @param ids raw id list, nullable
     * @return normalized list, never null
     */
    private List<String> normalizeIds(List<String> ids) {
        return ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
    }

    /**
     * Find by primary key.
     *
     * @param operatorId operator user id
     * @param id         user id
     * @return aggregate
     */
    public JulyUser getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param operatorId     operator user id
     * @param pageQuery      page query, null falls back to page 1 / size 10
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword    user name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyUser> selectListByPage(String operatorId, PageQuery011 pageQuery, String accountKeyword,
            String nameKeyword) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyUser> rows = repository.findPage(query.offset(), query.pageSize(), accountKeyword, nameKeyword);
        long total = repository.count(accountKeyword, nameKeyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Reset a user password (admin action; the raw password is hashed).
     *
     * @param operatorId  operator user id
     * @param id          user id
     * @param rawPassword new raw password
     */
    @Transactional
    public void resetPassword(String operatorId, String id, String rawPassword) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.RESET_PASSWORD);
        JulyUser user = require(id);
        user.resetPassword(passwordPort.encode(rawPassword));
        repository.update(user);
    }

    /**
     * Login with account and password (bcrypt). Any environment.
     *
     * @param userAccount login account
     * @param password    raw password
     * @param ip          client IP for the audit record
     * @return login result with the signed token
     */
    @Transactional
    public LoginResult login(String userAccount, String password, String ip) {
        JulyUser user = repository.findByAccount(userAccount);

        if (user == null || !passwordPort.matches(password == null ? "" : password, user.password())) {
            recordFailed(userAccount, "wrong account or password", ip);
            throw BusinessException.wrongAccountOrPassword();
        }

        if (Status011.DISABLED.getCode().equals(user.status())) {
            recordFailed(userAccount, "account disabled", ip);
            throw BusinessException.unauthorized("account is disabled");
        }

        String token = authTokenPort.issue(user.id().value(), user.userAccount(), user.tokenVersion());
        repository.touchLastLoginTime(user.id().value());
        userAuditPort.record(user.id().value(), user.userAccount(), AuditType011.LOGIN, AuditObjectCodes011.JULY_USER, "login success", ip);

        return new LoginResult(token, user.userAccount(), user.userName(),
                userRoleCodesPort.findRoleCodes(user.id().value()));
    }

    /**
     * Passwordless login by account only; permitted in debug / development
     * runtime modes only (production rejects it).
     *
     * @param userAccount login account
     * @param ip          client IP for the audit record
     * @return login result with the signed token
     */
    @Transactional
    public LoginResult loginByUserName(String userAccount, String ip) {
        if (!runtimeStatusPort.allowsPasswordlessLogin()) {
            throw BusinessException.unauthorized("passwordless login is not allowed in production");
        }

        JulyUser user = repository.findByAccount(userAccount);

        if (user == null) {
            recordFailed(userAccount, "wrong account or password", ip);
            throw BusinessException.wrongAccountOrPassword();
        }

        if (Status011.DISABLED.getCode().equals(user.status())) {
            recordFailed(userAccount, "account disabled", ip);
            throw BusinessException.unauthorized("account is disabled");
        }

        String token = authTokenPort.issue(user.id().value(), user.userAccount(), user.tokenVersion());
        repository.touchLastLoginTime(user.id().value());
        userAuditPort.record(user.id().value(), user.userAccount(), AuditType011.LOGIN, AuditObjectCodes011.JULY_USER, "passwordless login", ip);

        return new LoginResult(token, user.userAccount(), user.userName(),
                userRoleCodesPort.findRoleCodes(user.id().value()));
    }

    /**
     * Change the password of the current operator: verify the old password,
     * then replace the hash. The operator id comes from the auth filter (via
     * the controller).
     *
     * @param operatorId  current operator user id
     * @param oldPassword old raw password
     * @param newPassword new raw password
     */
    @Transactional
    public void changePassword(String operatorId, String oldPassword, String newPassword) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.CHANGE_PASSWORD);

        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized("not authenticated");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw BusinessException.badRequest("change password: new password is required");
        }

        JulyUser user = require(operatorId);

        if (!passwordPort.matches(oldPassword == null ? "" : oldPassword, user.password())) {
            throw BusinessException.badRequest("old password is wrong");
        }

        user.resetPassword(passwordPort.encode(newPassword));
        repository.update(user);
        userAuditPort.record(operatorId, user.userAccount(), AuditType011.CHANGE_PASSWORD, AuditObjectCodes011.JULY_USER, "password changed",
                null);
    }

    /**
     * Export all user rows (permission-gated; payload is export result only).
     *
     * @param operator authenticated operator
     * @return export result
     */
    public ExportResult export(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyUserPermissionCodes011.EXPORT);
        return exportUseCase.export(AuditObjectCodes011.JULY_USER, operator);
    }

    /**
     * Backup all user rows to object storage (permission-gated). Returns the
     * storage object key only — never the user row payload.
     *
     * @param operator authenticated operator
     * @return storage object key
     */
    public String backup(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyUserPermissionCodes011.BACKUP);
        return backupUseCase.backup(AuditObjectCodes011.JULY_USER, operator);
    }

    /**
     * Record a failed login into the audit trail.
     *
     * @param userAccount attempted account
     * @param reason      failure reason
     * @param ip          client IP
     */
    private void recordFailed(String userAccount, String reason, String ip) {
        userAuditPort.record(null, userAccount, AuditType011.LOGIN_FAILED, AuditObjectCodes011.JULY_USER, reason, ip);
    }

    /**
     * Logout of the current operator: bump {@code tokenVersion} so the current
     * JWT (and any other sessions) fail verify, then record the LOGOUT audit
     * row. The client should still clear its copy of the token.
     *
     * @param operatorId  current operator user id, nullable (debug no-token)
     * @param userAccount current operator account, nullable
     * @param ip          client IP
     */
    @Transactional
    public void logout(String operatorId, String userAccount, String ip) {
        if (operatorId == null || operatorId.isBlank()) {
            return;
        }

        JulyUser user = repository.findById(operatorId);

        if (user != null) {
            user.revokeTokens();
            repository.update(user);
        }

        userAuditPort.record(operatorId, userAccount, AuditType011.LOGOUT, AuditObjectCodes011.JULY_USER, "logout", ip);
    }

    /**
     * Require an authenticated operator.
     *
     * @param operator operator
     */
    private void requireOperator(Operator011 operator) {
        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized("not authenticated");
        }
    }

    /**
     * Require an existing user.
     *
     * @param id user id
     * @return aggregate
     */
    private JulyUser require(String id) {
        JulyUser user = repository.findById(id);

        if (user == null) {
            throw BusinessException.recordNotFound(id);
        }

        return user;
    }
}

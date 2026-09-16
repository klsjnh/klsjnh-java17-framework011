package com.klsjnh.application.iam;

/*                JulyUserUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user use case class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteErrorVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.iam.AuthTokenPort;
import com.klsjnh.domain.iam.JulyUser;
import com.klsjnh.domain.iam.JulyUserRepository;
import com.klsjnh.domain.iam.JulyRoleRepository;
import com.klsjnh.domain.iam.JulyUserRoleRepository;
import com.klsjnh.domain.iam.PasswordPort;
import com.klsjnh.domain.iam.RuntimeStatusPort;
import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JulyUser use cases: user CRUD, role assignment (toggle), password reset and
 * the two login kinds (account+password / passwordless by account, gated by
 * krt.status).
 */

@Service
public class JulyUserUseCase {

    /**
     * JulyUser repository.
     */
    private final JulyUserRepository repository;

    /**
     * User role junction repository.
     */
    private final JulyUserRoleRepository userRoleRepository;

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
     * JulyRole repository.
     */
    private final JulyRoleRepository roleRepository;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param repository         july user repository
     * @param userRoleRepository user role junction repository
     * @param passwordPort       password hashing port
     * @param authTokenPort      auth token port
     * @param runtimeStatusPort  runtime status port
     * @param userAuditPort      user audit port
     */
    public JulyUserUseCase(JulyUserRepository repository, JulyUserRoleRepository userRoleRepository,
            PasswordPort passwordPort, AuthTokenPort authTokenPort, RuntimeStatusPort runtimeStatusPort,
            JulyRoleRepository roleRepository, UserAuditPort userAuditPort) {
        this.repository = repository;
        this.userRoleRepository = userRoleRepository;
        this.passwordPort = passwordPort;
        this.authTokenPort = authTokenPort;
        this.runtimeStatusPort = runtimeStatusPort;
        this.roleRepository = roleRepository;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Insert a new user (password is hashed with bcrypt).
     *
     * @param userAccount login account, unique
     * @param userName    user name
     * @param password    raw password
     * @param mobile      mobile number, nullable
     * @param email       email, nullable
     * @return new user id
     */
    @Transactional
    public String insert(String userAccount, String userName, String password, String mobile, String email,
            String avatar, String pkOrg) {
        if (password == null || password.isBlank()) {
            throw BusinessException.badRequest("insert: password is required");
        }

        if (repository.findByAccount(userAccount) != null) {
            throw BusinessException.badRequest("user account already exists: " + userAccount);
        }

        JulyUser user = new JulyUser(EntityId.generate(), userAccount, userName, passwordPort.encode(password),
                mobile, email, avatar, pkOrg, null, Status011.ENABLED.getCode(), AuditInfo.empty());
        repository.insert(user);

        return user.id().value();
    }

    /**
     * Update the profile (account and password are not part of profile updates).
     *
     * @param id       user id
     * @param userName user name
     * @param mobile   mobile number
     * @param email    email
     */
    @Transactional
    public void update(String id, String userName, String mobile, String email, String avatar, String pkOrg) {
        JulyUser user = require(id);
        user.updateProfile(userName, mobile, email, avatar, pkOrg);
        repository.update(user);
    }

    /**
     * Logic delete a single user.
     *
     * @param id user id
     * @return deleted user id
     */
    @Transactional
    public String logicDelete(String id) {
        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Logic delete users (batch), reporting per-id success and failure.
     *
     * <p>Note: the user_role junction rows are NOT cascaded here — role
     * assignment is a separate aggregate and the relation is inactive as soon
     * as the user is deleted.</p>
     *
     * @param ids user ids
     * @return per-id success/failure summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        List<String> normalized = normalizeIds(ids);
        result.setTotal(normalized.size());

        for (String id : normalized) {
            boolean deleted = repository.logicDeleteById(id);

            if (deleted) {
                result.setSuccess(result.getSuccess() + 1);
            } else {
                result.setFailed(result.getFailed() + 1);
                BatchDeleteErrorVo011 error = new BatchDeleteErrorVo011();
                error.setId(id);
                error.setMessage("record not found");
                result.getErrors().add(error);
            }
        }

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
     * @param id user id
     * @return aggregate
     */
    public JulyUser getById(String id) {
        return require(id);
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param pageQuery      page query, null falls back to page 1 / size 10
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword    user name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyUser> selectListByPage(PageQuery011 pageQuery, String accountKeyword,
            String nameKeyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyUser> rows = repository.findPage(query.offset(), query.pageSize(), accountKeyword, nameKeyword);
        long total = repository.count(accountKeyword, nameKeyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Assign roles to a user (toggle semantics: granted roles are revived,
     * revoked roles are stopped).
     *
     * @param id      user id
     * @param pkRoles role ids to grant
     */
    @Transactional
    public void assignRoles(String id, List<String> pkRoles) {
        require(id);

        List<String> desired = pkRoles == null ? List.of()
                : pkRoles.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
        List<String> current = userRoleRepository.findRoleIds(id);

        for (String pkRole : desired) {
            if (!current.contains(pkRole)) {
                userRoleRepository.assign(id, pkRole);
            }
        }

        for (String pkRole : current) {
            if (!desired.contains(pkRole)) {
                userRoleRepository.unassign(id, pkRole);
            }
        }
    }

    /**
     * Reset a user password (admin action; the raw password is hashed).
     *
     * @param id          user id
     * @param rawPassword new raw password
     */
    @Transactional
    public void resetPassword(String id, String rawPassword) {
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

        String token = authTokenPort.issue(user.id().value(), user.userAccount());
        repository.touchLastLoginTime(user.id().value());
        userAuditPort.record(user.id().value(), user.userAccount(), AuditType011.LOGIN, "july_user", "login success", ip);

        return new LoginResult(token, user.userAccount(), user.userName(), currentRoleCodes(user.id().value()));
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

        String token = authTokenPort.issue(user.id().value(), user.userAccount());
        repository.touchLastLoginTime(user.id().value());
        userAuditPort.record(user.id().value(), user.userAccount(), AuditType011.LOGIN, "july_user", "passwordless login", ip);

        return new LoginResult(token, user.userAccount(), user.userName(), currentRoleCodes(user.id().value()));
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
        userAuditPort.record(operatorId, user.userAccount(), AuditType011.CHANGE_PASSWORD, "july_user", "password changed",
                null);
    }

    /**
     * Record a failed login into the audit trail.
     *
     * @param userAccount attempted account
     * @param reason      failure reason
     * @param ip          client IP
     */
    private void recordFailed(String userAccount, String reason, String ip) {
        userAuditPort.record(null, userAccount, AuditType011.LOGIN_FAILED, "july_user", reason, ip);
    }

    /**
     * Logout of the current operator: stateless JWT cannot be revoked, so the
     * server side only records the LOGOUT audit row — the client clears the
     * token.
     *
     * @param operatorId  current operator user id, nullable (debug no-token)
     * @param userAccount current operator account, nullable
     * @param ip          client IP
     */
    @Transactional
    public void logout(String operatorId, String userAccount, String ip) {
        if (operatorId != null && !operatorId.isBlank()) {
            userAuditPort.record(operatorId, userAccount, AuditType011.LOGOUT, "july_user", "logout", ip);
        }
    }

    /**
     * Role codes currently granted to a user.
     *
     * @param userId user id
     * @return role code list
     */
    private List<String> currentRoleCodes(String userId) {
        return roleRepository.findCodesByIds(userRoleRepository.findRoleIds(userId));
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

package com.klsjnh.domain.iam.user;

/*                JulyUser class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user class
 *      2026.09.26  tokenVersion for jwt revoke; changeStatus bumps
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JulyUser aggregate root (system management context): a login account and its
 * profile. Runtime status reuses the common status column values — transitions
 * go through {@link Status011}. {@code tokenVersion} is bumped on credential
 * invalidating events so previously issued JWTs fail verify.
 */

public class JulyUser {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Login account, unique, immutable after create.
     */
    private final String userAccount;

    /**
     * User name.
     */
    private String userName;

    /**
     * Password hash (bcrypt), never exposed outside the persistence boundary.
     */
    private String password;

    /**
     * Mobile number.
     */
    private String mobile;

    /**
     * Email.
     */
    private String email;

    /**
     * Avatar.
     */
    private String avatar;

    /**
     * Organization link (pk_org), nullable — the user may belong to no
     * organization.
     */
    private String pkOrg;

    /**
     * Last login time.
     */
    private LocalDateTime lastLoginTime;

    /**
     * JWT credential version: embedded in issued tokens as claim {@code tv};
     * bump on status change / password change / logout so old tokens fail.
     */
    private int tokenVersion;

    /**
     * Account status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id            primary key
     * @param userAccount   login account, unique
     * @param userName      user name
     * @param password      password hash (bcrypt)
     * @param mobile        mobile number
     * @param email         email
     * @param avatar        avatar
     * @param pkOrg         organization link, nullable
     * @param lastLoginTime last login time
     * @param tokenVersion  jwt credential version
     * @param status        account status
     * @param audit         audit info
     */
    public JulyUser(EntityId id, String userAccount, String userName, String password, String mobile, String email,
            String avatar, String pkOrg, LocalDateTime lastLoginTime, int tokenVersion, String status,
            AuditInfo audit) {
        this.id = id;
        this.userAccount = userAccount;
        this.userName = userName;
        this.password = password;
        this.mobile = mobile;
        this.email = email;
        this.avatar = avatar;
        this.pkOrg = pkOrg;
        this.lastLoginTime = lastLoginTime;
        this.tokenVersion = Math.max(0, tokenVersion);
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new user: defaults to enabled with a bcrypt password hash
     * and token version 0.
     *
     * @param id          primary key
     * @param userAccount login account, max 30
     * @param userName    user name, max 60
     * @param password    password hash (bcrypt), max 100
     * @param audit       audit info
     * @return new aggregate in enabled state
     */
    public static JulyUser create(EntityId id, String userAccount, String userName, String password, AuditInfo audit) {
        validate(userAccount, userName, password);
        return new JulyUser(id, userAccount, userName, password, null, null, null, null, null, 0,
                Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the profile (account and password are not part of profile updates).
     *
     * @param userName user name
     * @param mobile   mobile number
     * @param email    email
     * @param avatar   avatar
     * @param pkOrg    organization link, nullable
     */
    public void updateProfile(String userName, String mobile, String email, String avatar, String pkOrg) {
        if (userName == null || userName.isBlank() || userName.length() > 60) {
            throw new IllegalArgumentException("user name is required (max 60)");
        }

        this.userName = userName;
        this.mobile = mobile;
        this.email = email;
        this.avatar = avatar;
        this.pkOrg = pkOrg;
    }

    /**
     * Replace the password hash (the raw password is hashed by the use case)
     * and bump the token version so existing JWTs fail verify.
     *
     * @param passwordHash new bcrypt hash
     */
    public void resetPassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }

        this.password = passwordHash;
        bumpTokenVersion();
    }

    /**
     * Switch the account to enabled and bump the token version.
     */
    public void enable() {
        changeStatus(Status011.ENABLED.getCode());
    }

    /**
     * Switch the account to disabled and bump the token version.
     */
    public void disable() {
        changeStatus(Status011.DISABLED.getCode());
    }

    /**
     * Apply an explicit status value (used by the update path). Resolves the
     * raw column value and bumps the token version when the status actually
     * changes so previously issued JWTs fail verify.
     *
     * @param status raw status column value ("1" / "0")
     */
    public void changeStatus(String status) {
        Status011 next = Status011.of(status);

        if (next == null) {
            throw new IllegalArgumentException("invalid user status: " + status);
        }

        String nextCode = next.getCode();

        if (Objects.equals(this.status, nextCode)) {
            return;
        }

        this.status = nextCode;
        bumpTokenVersion();
    }

    /**
     * Invalidate all outstanding JWTs for this user (logout / explicit revoke).
     */
    public void revokeTokens() {
        bumpTokenVersion();
    }

    /**
     * Increment the credential version (overflow wraps within int range).
     */
    private void bumpTokenVersion() {
        this.tokenVersion++;
    }

    /**
     * Refresh the last login time.
     *
     * @param time login time
     */
    public void touchLastLoginTime(LocalDateTime time) {
        this.lastLoginTime = time;
    }

    /**
     * Validate the create basics.
     *
     * @param userAccount login account
     * @param userName    user name
     * @param password    password hash
     */
    private static void validate(String userAccount, String userName, String password) {
        StringUtil011.requirePresent(userAccount, "user account", 30);

        StringUtil011.requirePresent(userName, "user name", 60);

        StringUtil011.requirePresent(password, "password hash", 100);
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the login account.
     *
     * @return login account
     */
    public String userAccount() {
        return userAccount;
    }

    /**
     * Get the user name.
     *
     * @return user name
     */
    public String userName() {
        return userName;
    }

    /**
     * Get the password hash.
     *
     * @return bcrypt hash
     */
    public String password() {
        return password;
    }

    /**
     * Get the mobile number.
     *
     * @return mobile number
     */
    public String mobile() {
        return mobile;
    }

    /**
     * Get the email.
     *
     * @return email
     */
    public String email() {
        return email;
    }

    /**
     * Get the avatar.
     *
     * @return avatar
     */
    public String avatar() {
        return avatar;
    }

    /**
     * Get the organization link.
     *
     * @return organization id or null
     */
    public String pkOrg() {
        return pkOrg;
    }

    /**
     * Get the last login time.
     *
     * @return last login time
     */
    public LocalDateTime lastLoginTime() {
        return lastLoginTime;
    }

    /**
     * Get the JWT credential version.
     *
     * @return token version
     */
    public int tokenVersion() {
        return tokenVersion;
    }

    /**
     * Get the account status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}

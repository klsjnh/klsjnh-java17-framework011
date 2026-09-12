package com.klsjnh.domain.iam;

/*                JulyUser class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.time.LocalDateTime;

/**
 * JulyUser aggregate root (system management context): a login account and its
 * profile. Runtime status reuses the common status column values — transitions
 * go through {@link Status011}.
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
     * Last login time.
     */
    private LocalDateTime lastLoginTime;

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
     * @param id        primary key
     * @param userAccount login account, unique
     * @param userName  user name
     * @param password  password hash (bcrypt)
     * @param mobile    mobile number
     * @param email     email
     * @param lastLoginTime last login time
     * @param status    account status
     * @param audit     audit info
     */
    public JulyUser(EntityId id, String userAccount, String userName, String password, String mobile, String email,
            LocalDateTime lastLoginTime, String status, AuditInfo audit) {
        this.id = id;
        this.userAccount = userAccount;
        this.userName = userName;
        this.password = password;
        this.mobile = mobile;
        this.email = email;
        this.lastLoginTime = lastLoginTime;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new user: defaults to enabled with a bcrypt password hash.
     *
     * @param id        primary key
     * @param userAccount login account, max 30
     * @param userName  user name, max 60
     * @param password  password hash (bcrypt), max 100
     * @param audit     audit info
     * @return new aggregate in enabled state
     */
    public static JulyUser create(EntityId id, String userAccount, String userName, String password, AuditInfo audit) {
        validate(userAccount, userName, password);
        return new JulyUser(id, userAccount, userName, password, null, null, null, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the profile (account and password are not part of profile updates).
     *
     * @param userName user name
     * @param mobile   mobile number
     * @param email    email
     */
    public void updateProfile(String userName, String mobile, String email) {
        if (userName == null || userName.isBlank() || userName.length() > 60) {
            throw new IllegalArgumentException("user name is required (max 60)");
        }

        this.userName = userName;
        this.mobile = mobile;
        this.email = email;
    }

    /**
     * Replace the password hash (the raw password is hashed by the use case).
     *
     * @param passwordHash new bcrypt hash
     */
    public void resetPassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }

        this.password = passwordHash;
    }

    /**
     * Switch the account to enabled.
     */
    public void enable() {
        this.status = Status011.ENABLED.getCode();
    }

    /**
     * Switch the account to disabled.
     */
    public void disable() {
        this.status = Status011.DISABLED.getCode();
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
        if (StringUtil011.isMissing(userAccount, 30)) {
            throw new IllegalArgumentException("user account is required (max 30)");
        }

        if (StringUtil011.isMissing(userName, 60)) {
            throw new IllegalArgumentException("user name is required (max 60)");
        }

        if (StringUtil011.isMissing(password, 100)) {
            throw new IllegalArgumentException("password hash is required (max 100)");
        }
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
     * Get the last login time.
     *
     * @return last login time
     */
    public LocalDateTime lastLoginTime() {
        return lastLoginTime;
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

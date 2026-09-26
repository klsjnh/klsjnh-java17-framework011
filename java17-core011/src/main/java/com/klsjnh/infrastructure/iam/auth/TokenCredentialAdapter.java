package com.klsjnh.infrastructure.iam.auth;

/*                TokenCredentialAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july_user backed token credential lookup
 *
 */

import com.klsjnh.domain.iam.auth.TokenCredentialPort;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;

import org.springframework.stereotype.Component;

/**
 * {@link TokenCredentialPort} adapter: loads {@code token_version} and
 * {@code status} from {@link JulyUserRepository} for JWT verify.
 */

@Component
public class TokenCredentialAdapter implements TokenCredentialPort {

    /**
     * July user repository.
     */
    private final JulyUserRepository julyUserRepository;

    /**
     * Create the adapter.
     *
     * @param julyUserRepository july user repository
     */
    public TokenCredentialAdapter(JulyUserRepository julyUserRepository) {
        this.julyUserRepository = julyUserRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CredentialState findByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        JulyUser user = julyUserRepository.findById(userId);

        if (user == null) {
            return null;
        }

        return new CredentialState(user.tokenVersion(), user.status());
    }
}

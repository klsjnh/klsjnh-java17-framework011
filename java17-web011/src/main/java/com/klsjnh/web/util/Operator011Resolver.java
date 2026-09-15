package com.klsjnh.web.util;

/*                Operator011Resolver class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  operator resolver for controller audit parameters
 *
 */

import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.identity.Operator011;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Turns the current request into the {@link Operator011} the application layer
 * expects, so a controller never hand-picks the id / account / ip attributes
 * again.
 * <p>
 * GlobalAuthFilter has already verified the token and published both the id
 * and the account as request attributes; this resolver simply reads them back
 * alongside the client IP.
 * </p>
 */

public final class Operator011Resolver {

    /**
     * Utility class, no instances.
     */
    private Operator011Resolver() {
    }

    /**
     * Read the authenticated operator out of a request.
     *
     * @param request http request, nullable (returns null when absent)
     * @return operator, or null when the request is null
     */
    public static Operator011 resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String id = (String) request.getAttribute(FrameConst011.OPERATOR_ID);
        String userAccount = (String) request.getAttribute(FrameConst011.OPERATOR_ACCOUNT);

        return new Operator011(id, userAccount, ClientIp011Resolver.resolve(request));
    }
}

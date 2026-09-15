package com.klsjnh.common.identity;

/*                Operator011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  operator identity carrier for the audit trail
 *
 */

/**
 * The authenticated operator of the current request, carried down from the web
 * layer into the application layer as one value.
 * <p>
 * This exists because passing the three parts (id / account / ip) as separate
 * parameters lets a caller silently drop one — the logout and export paths did
 * exactly that, leaving audit rows with no operator. Bundling them makes the
 * identity travel as a single required argument.
 * </p>
 * <p>
 * It belongs to common, not domain: an operator identity is request context,
 * not a domain concept. This class holds no servlet type — the servlet-aware
 * resolver (which turns an HttpServletRequest into this object) lives in the
 * web layer, keeping common free of a servlet dependency.
 * </p>
 *
 * @param id          operator user id, nullable for an anonymous request
 * @param userAccount operator login account, nullable for an anonymous request
 * @param ip          client IP, nullable
 */

public record Operator011(String id, String userAccount, String ip) {

    /**
     * Whether this operator carries an authenticated id.
     *
     * @return true when the id is present and not blank
     */
    public boolean authenticated() {
        return id != null && !id.isBlank();
    }

    /**
     * An anonymous operator (no id, no account) from the given client IP.
     *
     * @param ip client IP, nullable
     * @return anonymous operator
     */
    public static Operator011 anonymous(String ip) {
        return new Operator011(null, null, ip);
    }
}

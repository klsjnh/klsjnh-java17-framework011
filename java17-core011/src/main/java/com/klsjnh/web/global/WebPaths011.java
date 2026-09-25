package com.klsjnh.web.global;

/*                WebPaths011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  web path constants shared by the auth filter and controllers
 *      2026.09.26  message inbound receive path (JWT whitelist example)
 *
 */

/**
 * Web path constants shared between {@link GlobalAuthFilter} and the
 * controllers whose paths it must whitelist.
 * <p>
 * Why a constant: the login whitelist is matched against the raw request URI,
 * so a controller module move (e.g. {@code system011 → iam}) silently breaks
 * authentication — the whitelist keeps the old path and the login endpoint
 * itself gets a 401 outside debug. Declaring the base path once, and using it
 * in both the controller mapping and the whitelist, makes that drift a compile
 * error instead of a production incident.
 * </p>
 */

public final class WebPaths011 {

    /**
     * IAM user module base path ({@code JulyUserController}).
     */
    public static final String IAM_USER = "/klsjnh/iam/julyUser/v1";

    /**
     * Message-center inbound message module base path
     * ({@code JulyInboundMessageController}).
     */
    public static final String MESSAGE_INBOUND_MESSAGE = "/klsjnh/messagecenter/julyInboundMessage/v1";

    /**
     * Unified inbound receive endpoint (vendor HTTP callback). Hosts that need
     * anonymous vendor callbacks add this exact path to
     * {@code krt.web.auth-whitelist-paths}; channel-side signature verification
     * stays in {@code MessageInboundPort.parse} / channel config.
     */
    public static final String MESSAGE_INBOUND_RECEIVE = MESSAGE_INBOUND_MESSAGE + "/receive";

    /**
     * Not instantiable.
     */
    private WebPaths011() {
    }
}

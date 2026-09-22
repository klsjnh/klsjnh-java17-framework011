package com.klsjnh.web.global;

/*                WebPaths011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  web path constants shared by the auth filter and controllers
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
     * Not instantiable.
     */
    private WebPaths011() {
    }
}

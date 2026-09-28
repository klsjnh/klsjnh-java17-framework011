package com.klsjnh.domain.iam.auth;

/*                AccessCenterMarker011 interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  classpath marker for center-access011-starter
 *
 */

/**
 * Marker present only when {@code center-access011-starter} is on the
 * classpath and registers its bean. Security uses it to enable the production
 * write-operation permission whitelist gate; without the marker the gate stays
 * off so user-only assemblies are not forced to call {@code assertHas}.
 * <p>
 * A user-only assembly is <b>not</b> silently permissive: the fallback
 * {@code PermissiveAuthorizationPort} requires the explicit
 * {@code krt.security.permissive=true} opt-in and refuses to start outside
 * debug. Missing both the marker and the opt-in fails the context at startup
 * (fail-closed), by design.
 * </p>
 */

public interface AccessCenterMarker011 {
}

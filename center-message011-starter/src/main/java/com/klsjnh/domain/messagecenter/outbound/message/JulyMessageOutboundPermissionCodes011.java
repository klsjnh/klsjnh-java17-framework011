package com.klsjnh.domain.messagecenter.outbound.message;

/*                JulyMessageOutboundPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july message outbound permission codes
 *
 */

/**
 * Permission codes for julyMessageOutbound (send records).
 */

public final class JulyMessageOutboundPermissionCodes011 {

    public static final String SELECT = "messagecenter:julyMessageOutbound:select";
    public static final String SEND = "messagecenter:julyMessageOutbound:send";
    public static final String RESEND = "messagecenter:julyMessageOutbound:resend";
    public static final String LOGIC_DELETE = "messagecenter:julyMessageOutbound:logicDelete";

    private JulyMessageOutboundPermissionCodes011() {
    }
}

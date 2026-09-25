package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july ai domain + prompt permission codes
 *
 */

/**
 * Permission codes for julyAiDomain master-sub management (domain tree + prompts).
 */

public final class JulyAiDomainPermissionCodes011 {

    /** View (domain / prompt reads, tree, content). */
    public static final String SELECT = "aicenter:julyAiDomain:select";

    /** Insert (domain / prompt / saveWhole insert). */
    public static final String INSERT = "aicenter:julyAiDomain:insert";

    /** Update (domain / prompt / saveWhole update). */
    public static final String UPDATE = "aicenter:julyAiDomain:update";

    /** Logic delete (domain / prompt). */
    public static final String LOGIC_DELETE = "aicenter:julyAiDomain:logicDelete";

    /** Render a prompt by code. */
    public static final String RENDER = "aicenter:julyAiDomain:render";

    private JulyAiDomainPermissionCodes011() {
    }
}

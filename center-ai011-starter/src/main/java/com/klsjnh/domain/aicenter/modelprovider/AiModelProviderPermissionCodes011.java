package com.klsjnh.domain.aicenter.modelprovider;

/*                AiModelProviderPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july ai model provider permission codes
 *
 */

/**
 * Permission codes for julyAiModelProvider management (provider + nested api keys).
 */

public final class AiModelProviderPermissionCodes011 {

    /** View (getById / selectListByPage / selectApiListByProvider / getWithChildren). */
    public static final String SELECT = "aicenter:julyAiModelProvider:select";

    /** Insert (provider / api key / saveWhole insert). */
    public static final String INSERT = "aicenter:julyAiModelProvider:insert";

    /** Update (provider / api key / saveWhole update). */
    public static final String UPDATE = "aicenter:julyAiModelProvider:update";

    /** Logic delete (provider / api key). */
    public static final String LOGIC_DELETE = "aicenter:julyAiModelProvider:logicDelete";

    /** Connectivity probe (testConnection / testConnectionApi). */
    public static final String TEST_CONNECTION = "aicenter:julyAiModelProvider:testConnection";

    /** Export all providers (no api keys). */
    public static final String EXPORT = "aicenter:julyAiModelProvider:export";

    private AiModelProviderPermissionCodes011() {
    }
}

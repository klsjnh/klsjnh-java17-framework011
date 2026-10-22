package com.klsjnh.domain.aicenter.file;

/*                AiFilePermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  permission codes for provider file assets
 *
 */

/**
 * Permission codes for AI provider file assets.
 */

public final class AiFilePermissionCodes011 {

    /** Upload a provider file. */
    public static final String UPLOAD = "aicenter:aiFile:upload";

    /** Read file metadata / list. */
    public static final String SELECT = "aicenter:aiFile:select";

    /** Download file bytes. */
    public static final String CONTENT = "aicenter:aiFile:download";

    /** Delete a provider file. */
    public static final String DELETE = "aicenter:aiFile:delete";

    private AiFilePermissionCodes011() {
    }
}

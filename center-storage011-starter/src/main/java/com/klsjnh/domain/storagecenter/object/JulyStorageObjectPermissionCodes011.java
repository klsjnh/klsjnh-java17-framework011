package com.klsjnh.domain.storagecenter.object;

/*                JulyStorageObjectPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july storage object permission codes
 *
 */

/**
 * Permission codes for julyStorageObject operations (download/stat exempt at HTTP whitelist).
 */

public final class JulyStorageObjectPermissionCodes011 {

    public static final String SELECT = "storagecenter:julyStorageObject:select";
    public static final String UPLOAD = "storagecenter:julyStorageObject:upload";
    public static final String REMOVE = "storagecenter:julyStorageObject:remove";
    public static final String COPY = "storagecenter:julyStorageObject:copy";
    public static final String RENAME = "storagecenter:julyStorageObject:rename";
    public static final String SAVE_TEXT = "storagecenter:julyStorageObject:saveText";

    /**
     * Prevent instantiation.
     */
    private JulyStorageObjectPermissionCodes011() {
    }
}

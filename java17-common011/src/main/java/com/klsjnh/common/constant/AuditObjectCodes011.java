package com.klsjnh.common.constant;

/*                AuditObjectCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  audit object codes (table name -> lower camel)
 *
 */

/**
 * Audit / export / backup object codes: the table name in lower camel case.
 * The single vocabulary shared by {@code @AuditLog(objectCode = ...)},
 * {@code ExportUseCase.export(OBJECT_CODE, ...)} and
 * {@code BackupUseCase.backup(OBJECT_CODE, ...)}, so no caller hand-writes the
 * string (016 §6.5 open-vocabulary spirit).
 */

public final class AuditObjectCodes011 {

    /** july_ai_model_provider. */
    public static final String JULY_AI_MODEL_PROVIDER = "julyAiModelProvider";

    /** july_config. */
    public static final String JULY_CONFIG = "julyConfig";

    /** july_datasource. */
    public static final String JULY_DATASOURCE = "julyDatasource";

    /** july_dictionary. */
    public static final String JULY_DICTIONARY = "julyDictionary";

    /** july_menu. */
    public static final String JULY_MENU = "julyMenu";

    /** july_organization. */
    public static final String JULY_ORGANIZATION = "julyOrganization";

    /** july_role. */
    public static final String JULY_ROLE = "julyRole";

    /** july_scheduler. */
    public static final String JULY_SCHEDULER = "julyScheduler";

    /** july_storage_provider. */
    public static final String JULY_STORAGE_PROVIDER = "julyStorageProvider";

    /** july_storage_provider_bucket. */
    public static final String JULY_STORAGE_PROVIDER_BUCKET = "julyStorageProviderBucket";

    /** Storage object (not a table: an object inside a bucket). */
    public static final String JULY_STORAGE_OBJECT = "julyStorageObject";

    /** july_user. */
    public static final String JULY_USER = "julyUser";

    /**
     * Constant holder, no instances.
     */
    private AuditObjectCodes011() {
    }
}

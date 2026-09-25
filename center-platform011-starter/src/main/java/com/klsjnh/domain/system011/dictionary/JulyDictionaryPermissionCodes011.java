package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july dictionary permission codes
 *
 */

/**
 * Permission codes for julyDictionary management — must match catalog seed.
 */

public final class JulyDictionaryPermissionCodes011 {

    /**
     * View (getById / selectListByPage / selectItemListByType).
     */
    public static final String SELECT = "system011:julyDictionary:select";

    /**
     * Insert (dictionary + item insertItem).
     */
    public static final String INSERT = "system011:julyDictionary:insert";

    /**
     * Update (dictionary + item updateItem).
     */
    public static final String UPDATE = "system011:julyDictionary:update";

    /**
     * Logic delete (dictionary + item logicDeleteItem).
     */
    public static final String LOGIC_DELETE = "system011:julyDictionary:logicDelete";

    /**
     * Export.
     */
    public static final String EXPORT = "system011:julyDictionary:export";

    /**
     * Import.
     */
    public static final String IMPORT = "system011:julyDictionary:import";

    private JulyDictionaryPermissionCodes011() {
    }
}

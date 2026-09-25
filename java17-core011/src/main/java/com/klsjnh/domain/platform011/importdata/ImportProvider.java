package com.klsjnh.domain.platform011.importdata;

/*                ImportProvider interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import provider interface
 *
 */

import com.klsjnh.domain.platform011.export.ExportSheetSpec;

import java.util.List;

/**
 * Import provider port: one implementation per importable object. Controllers
 * are optional — registering a bean is enough for the platform ImportUseCase.
 */

public interface ImportProvider {

    /**
     * Unique object code (same vocabulary as export when paired).
     *
     * @return object code
     */
    String objectCode();

    /**
     * Expected sheets and column headers for decoding.
     *
     * @return sheet specs
     */
    List<ExportSheetSpec> sheetSpecs();

    /**
     * Apply a decoded bundle (upsert / replace per object rules). Must run
     * inside an application transaction owned by the use case or the provider's
     * use-case delegate.
     *
     * @param bundle decoded sheets
     * @return import counts
     */
    ImportResult apply(ImportBundle bundle);
}

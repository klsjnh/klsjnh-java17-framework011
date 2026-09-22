package com.klsjnh.domain.platform011.importdata;

/*                ImportBundle class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import bundle class
 *
 */

import java.util.List;
import java.util.Optional;

/**
 * Decoded import payload for one object.
 *
 * @param objectCode object code
 * @param sheets     sheets by name order from the file / specs
 */

public record ImportBundle(String objectCode, List<ImportSheet> sheets) {

    /**
     * Compact constructor: defensive copy.
     *
     * @param objectCode object code
     * @param sheets     sheets
     */
    public ImportBundle {
        sheets = sheets == null ? List.of() : List.copyOf(sheets);
    }

    /**
     * Find a sheet by name.
     *
     * @param name sheet name
     * @return sheet or empty
     */
    public Optional<ImportSheet> sheet(String name) {
        return sheets.stream().filter(s -> s.name().equals(name)).findFirst();
    }
}

package com.klsjnh.domain.lowcode011.records;

/*                MetadataContentMapper class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata content mapper class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadata;

/**
 * The <b>single</b> mapping between {@link MetadataContent} and the engine
 * aggregate {@code JulyMetadata}. The former modeling value-object family is
 * gone; the contract is now the only schema, so this mapper is the one place
 * that bridges aggregate and content.
 */

public final class MetadataContentMapper {

    /**
     * Utility: no instances.
     */
    private MetadataContentMapper() {
    }

    /**
     * Project the engine aggregate onto the content contract.
     *
     * @param metadata engine aggregate
     * @return content contract
     */
    public static MetadataContent from(JulyMetadata metadata) {
        return new MetadataContent(metadata.objectName(), metadata.objectType(), metadata.description(),
                metadata.businessField(), metadata.packageName(), metadata.routerPath(), metadata.fields(),
                metadata.displays(), metadata.services());
    }
}

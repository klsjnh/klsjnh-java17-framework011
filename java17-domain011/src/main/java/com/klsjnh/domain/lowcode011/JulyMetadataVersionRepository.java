package com.klsjnh.domain.lowcode011;

/*                JulyMetadataVersionRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  publish snapshot repository interface
 *
 */

import java.util.List;

/**
 * Repository for the publish snapshots of a low-code object.
 */

public interface JulyMetadataVersionRepository {

    /**
     * Insert a new snapshot.
     *
     * @param version snapshot
     */
    void insert(JulyMetadataVersion version);

    /**
     * Find the latest snapshot of an object by publish time.
     *
     * @param objectName object name
     * @return latest snapshot or null
     */
    JulyMetadataVersion findLatest(String objectName);

    /**
     * Find one snapshot by object name and version.
     *
     * @param objectName object name
     * @param version    version
     * @return snapshot or null
     */
    JulyMetadataVersion findByObjectNameAndVersion(String objectName, String version);

    /**
     * Whether the object name + version pair already exists.
     *
     * @param objectName object name
     * @param version    version
     * @return true when present
     */
    boolean existsByObjectNameAndVersion(String objectName, String version);

    /**
     * List all snapshots of an object, newest first.
     *
     * @param objectName object name
     * @return snapshots
     */
    List<JulyMetadataVersion> listByObjectName(String objectName);
}

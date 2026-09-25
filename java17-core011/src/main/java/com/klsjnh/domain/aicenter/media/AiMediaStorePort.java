package com.klsjnh.domain.aicenter.media;

/*                AiMediaStorePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai media persistence port
 *      2026.09.24  require call-time storage locator (no default binding)
 *
 */

import com.klsjnh.domain.aicenter.capability.AiMedia;

/**
 * Persists a generated media artifact through the storage center ports and
 * returns a reference (storageCode / bucket / objectKey / url). The AI center
 * never manages the artifact lifecycle — when to delete is the consumer's
 * decision. The caller must pass an explicit {@link AiMediaLocation}; there is
 * no pre-bound default storage or default bucket.
 */

public interface AiMediaStorePort {

    /**
     * Store an artifact at the given location and return its reference.
     *
     * @param media    generated media (bytes / base64 / url)
     * @param location storage instance + bucket locator (required)
     * @return media reference, never null
     */
    AiMediaRef save(AiMedia media, AiMediaLocation location);
}

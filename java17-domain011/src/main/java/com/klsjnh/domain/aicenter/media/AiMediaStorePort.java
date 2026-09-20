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
 *
 */

import com.klsjnh.domain.aicenter.capability.AiMedia;

/**
 * Persists a generated media artifact through the storage center and returns a
 * reference (storage key / address). The AI center never manages the artifact
 * lifecycle — when to delete is the consumer's decision.
 */

public interface AiMediaStorePort {

    /**
     * Store an artifact and return its reference.
     *
     * @param media generated media (bytes / base64 / url)
     * @return media reference, never null
     */
    AiMediaRef save(AiMedia media);
}

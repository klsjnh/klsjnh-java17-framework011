package com.klsjnh.domain.shared;

/*                EntityId class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  entity id class
 *
 */

import java.util.UUID;

/**
 * Primary key value object: 32-char UUID string.
 *
 * @param value id string, never blank
 */

public record EntityId(String value) {

    /**
     * Create the id with validation.
     *
     * @param value id string
     */
    public EntityId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("entity id must not be blank");
        }
    }

    /**
     * Generate a new random id.
     *
     * @return generated id
     */
    public static EntityId generate() {
        return new EntityId(UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * Wrap an existing id string.
     *
     * @param value id string
     * @return id value object
     */
    public static EntityId of(String value) {
        return new EntityId(value);
    }
}

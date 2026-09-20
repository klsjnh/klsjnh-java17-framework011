package com.klsjnh.domain.aicenter.capability;

/*                AiCapabilityPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai capability marker interface
 *
 */

/**
 * Marker for every AI capability port (inference / image / tts / asr). The
 * capability code is an open string vocabulary; the registry routes by
 * (capability, provider) without knowing the concrete port type.
 */

public interface AiCapabilityPort {

    /**
     * The capability this port serves.
     *
     * @return capability code, never null
     */
    String capabilityCode();

    /**
     * Whether this port serves the given provider code.
     *
     * @param providerCode provider code
     * @return true when supported
     */
    boolean supports(String providerCode);

    /**
     * Whether this port is a generic fallback (serves any provider). The
     * registry prefers a vendor-specific port over a generic one, so a vendor
     * that adds its own SPI bean is never shadowed by the generic adapter.
     *
     * @return true when this is a generic fallback
     */
    default boolean generic() {
        return false;
    }
}

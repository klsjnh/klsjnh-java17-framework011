package com.klsjnh.domain.aicenter.capability;

/*                AiReturnTypes class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai artifact return type codes
 *
 */

/**
 * Built-in artifact return forms (open string vocabulary, not an enum): the
 * caller states how it wants the generated media back; the provider satisfies
 * it or fails loudly.
 */

public final class AiReturnTypes {

    /** Return a downloadable url. */
    public static final String URL = "url";

    /** Return the raw bytes. */
    public static final String BYTES = "bytes";

    /** Return a base64 string. */
    public static final String B64 = "b64";

    private AiReturnTypes() {
    }
}

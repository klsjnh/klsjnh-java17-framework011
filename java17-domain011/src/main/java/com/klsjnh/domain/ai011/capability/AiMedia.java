package com.klsjnh.domain.ai011.capability;

/*                AiMedia record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai media result record
 *
 */

/**
 * One generated media artifact (image / audio), carrying exactly one of the
 * three forms selected by {@code returnType}.
 *
 * @param returnType artifact form actually produced (URL / BYTES / B64)
 * @param url        download url when the form is URL, nullable otherwise
 * @param bytes      raw bytes when the form is BYTES, nullable otherwise
 * @param base64     base64 string when the form is B64, nullable otherwise
 * @param mimeType   media mime type (e.g. image/png, audio/mpeg), nullable
 */

public record AiMedia(String returnType, String url, byte[] bytes, String base64, String mimeType) {

    /**
     * A url artifact.
     *
     * @param url      download url
     * @param mimeType media mime type, nullable
     * @return artifact
     */
    public static AiMedia ofUrl(String url, String mimeType) {
        return new AiMedia(AiReturnTypes.URL, url, null, null, mimeType);
    }

    /**
     * A bytes artifact.
     *
     * @param bytes    raw bytes
     * @param mimeType media mime type, nullable
     * @return artifact
     */
    public static AiMedia ofBytes(byte[] bytes, String mimeType) {
        return new AiMedia(AiReturnTypes.BYTES, null, bytes, null, mimeType);
    }

    /**
     * A base64 artifact.
     *
     * @param base64   base64 string
     * @param mimeType media mime type, nullable
     * @return artifact
     */
    public static AiMedia ofBase64(String base64, String mimeType) {
        return new AiMedia(AiReturnTypes.B64, null, null, base64, mimeType);
    }
}

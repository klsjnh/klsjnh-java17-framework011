package com.klsjnh.domain.aicenter.inference;

/*                AiChatContentPartTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  multimodal content part validation tests
 *
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AiChatContentPart}: shape, scheme and payload rules.
 */

class AiChatContentPartTest {

    /**
     * Small valid png data url (1x1 transparent pixel).
     */
    private static final String PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8"
            + "z8DwHwAFAAH/q842iQAAAABJRU5ErkJggg==";

    @Test
    void textPartKeepsText() {
        AiChatContentPart part = AiChatContentPart.text("hello");

        assertEquals(AiChatContentPart.TYPE_TEXT, part.type());
        assertEquals("hello", part.text());
    }

    @Test
    /**
     * Image part keeps the URL and its type.
     */
    void imagePartKeepsUrl() {
        AiChatContentPart part = AiChatContentPart.imageUrl(PNG);

        assertEquals(AiChatContentPart.TYPE_IMAGE_URL, part.type());
        assertEquals(PNG, part.imageUrl());
    }

    @Test
    /**
     * Rejects an unsupported part type.
     */
    void rejectsUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatContentPart("audio", "x", null));
    }

    @Test
    /**
     * Rejects a text part carrying an image URL.
     */
    void rejectsTextWithImageUrl() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatContentPart("text", "x", PNG));
    }

    @Test
    /**
     * Rejects an image part without a URL.
     */
    void rejectsImageWithoutUrl() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatContentPart("image_url", null, " "));
    }

    @Test
    /**
     * Accepts base64 data URLs and https references.
     */
    void acceptsDataUrlAndHttps() {
        assertTrue(AiChatContentPart.isSupportedImageUrl(PNG));
        assertTrue(AiChatContentPart.isSupportedImageUrl("https://example.com/a.png"));
    }

    @Test
    /**
     * Rejects plain http, file and relative references.
     */
    void rejectsPlainHttpFileAndRelative() {
        assertFalse(AiChatContentPart.isSupportedImageUrl("http://example.com/a.png"));
        assertFalse(AiChatContentPart.isSupportedImageUrl("file:///tmp/a.png"));
        assertFalse(AiChatContentPart.isSupportedImageUrl("a.png"));
        assertFalse(AiChatContentPart.isSupportedImageUrl(null));
    }

    @Test
    /**
     * Rejects a data URL whose payload is not base64.
     */
    void rejectsBrokenBase64() {
        String broken = "data:image/png;base64,not-base64!!";

        assertThrows(IllegalArgumentException.class, () -> AiChatContentPart.validateImageUrl(broken, 1024));
    }

    @Test
    /**
     * Rejects a data URL over the byte budget.
     */
    void rejectsOversizedDataUrl() {
        assertThrows(IllegalArgumentException.class, () -> AiChatContentPart.validateImageUrl(PNG, 4));
    }

    @Test
    /**
     * Does not apply the data URL budget to https.
     */
    void doesNotLimitHttpsUrlSize() {
        assertDoesNotThrow(() -> AiChatContentPart.validateImageUrl("https://example.com/a.png", 4));
    }

    @Test
    /**
     * Keeps the payload out of validation errors.
     */
    void doesNotEchoPayloadInError() {
        String broken = "data:image/png;base64,secretsecret!!";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiChatContentPart.validateImageUrl(broken, 1024));

        assertFalse(ex.getMessage().contains("secretsecret"));
    }
}

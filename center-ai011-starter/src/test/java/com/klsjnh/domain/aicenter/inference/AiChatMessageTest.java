package com.klsjnh.domain.aicenter.inference;

/*                AiChatMessageTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  multimodal chat message invariant tests
 *
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link AiChatMessage}: text/parts exclusivity and part rules.
 */

class AiChatMessageTest {

    @Test
    void keepsTextOnlyMessageCompatible() {
        AiChatMessage message = new AiChatMessage("user", "hello");

        assertEquals("hello", message.content());
        assertEquals(null, message.contentParts());
    }

    @Test
    /**
     * Accepts text plus image parts.
     */
    void acceptsTextAndImageParts() {
        List<AiChatContentPart> parts = List.of(AiChatContentPart.text("look"),
                AiChatContentPart.imageUrl("https://example.com/a.png"));

        AiChatMessage message = new AiChatMessage("user", null, parts);

        assertEquals(2, message.contentParts().size());
    }

    @Test
    /**
     * Rejects content and contentParts together.
     */
    void rejectsBothContentAndParts() {
        List<AiChatContentPart> parts = List.of(AiChatContentPart.text("look"));

        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage("user", "hello", parts));
    }

    @Test
    /**
     * Rejects a message with neither content nor parts.
     */
    void rejectsNeitherContentNorParts() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage("user", null, null));
    }

    @Test
    /**
     * Rejects an empty part list.
     */
    void rejectsEmptyParts() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage("user", null, List.of()));
    }

    @Test
    /**
     * Rejects an image-only part list.
     */
    void rejectsPartsWithoutText() {
        List<AiChatContentPart> parts = List.of(AiChatContentPart.imageUrl("https://example.com/a.png"));

        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage("user", null, parts));
    }

    @Test
    /**
     * Rejects more than twenty parts.
     */
    void rejectsTooManyParts() {
        List<AiChatContentPart> parts = new ArrayList<>();

        for (int i = 0; i < 21; i++) {
            parts.add(AiChatContentPart.text("t" + i));
        }

        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage("user", null, parts));
    }

    @Test
    /**
     * Rejects a blank role.
     */
    void rejectsBlankRole() {
        assertThrows(IllegalArgumentException.class, () -> new AiChatMessage(" ", "hello"));
    }
}

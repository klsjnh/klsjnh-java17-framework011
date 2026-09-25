package com.klsjnh.infrastructure.messagecenter.outbound.channel;

/*                WebhookMessageChannelTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate 2026.09.21
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  webhook json escaping regression (jackson)
 *
 */

import com.klsjnh.domain.messagecenter.outbound.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regression tests for {@link WebhookMessageChannel#toJson}: the body must be
 * RFC 8259 valid for any input (tab / control characters / line separators /
 * non-ASCII), which the old hand-written escaping could not guarantee.
 */

class WebhookMessageChannelTest {

    /**
     * Independent mapper used to parse the produced body back.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Channel under test.
     */
    private final WebhookMessageChannel channel = new WebhookMessageChannel();

    /**
     * Build a command with a config carrying a url.
     *
     * @param to      receiver
     * @param title   title
     * @param content content
     * @return command
     */
    private MessageCommand cmd(String to, String title, String content) {
        return new MessageCommand("webhook", to, "text", Map.of("url", "http://example.com/hook"), null, null, null,
                title, content);
    }

    /**
     * Parse the body back into an ordered map.
     *
     * @param json json body
     * @return parsed map
     * @throws Exception parse failure
     */
    private Map<String, String> parse(String json) throws Exception {
        return MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {
        });
    }

    /**
     * Assert every value survives a round trip.
     *
     * @param to      receiver
     * @param title   title
     * @param content content
     * @throws Exception parse failure
     */
    private void assertRoundTrip(String to, String title, String content) throws Exception {
        Map<String, String> parsed = parse(channel.toJson(cmd(to, title, content)));

        Assertions.assertEquals(to, parsed.get("to"));
        Assertions.assertEquals(title, parsed.get("title"));
        Assertions.assertEquals(content, parsed.get("content"));
    }

    /**
     * Plain ASCII keeps the to / title / content key order.
     *
     * @throws Exception parse failure
     */
    @Test
    void asciiKeepsKeyOrder() throws Exception {
        Map<String, String> parsed = parse(channel.toJson(cmd("u1", "T", "hello")));

        Assertions.assertEquals(new ArrayList<>(List.of("to", "title", "content")),
                new ArrayList<>(parsed.keySet()));
    }

    /**
     * Double quotes and backslashes are escaped.
     *
     * @throws Exception parse failure
     */
    @Test
    void quoteAndBackslashAreEscaped() throws Exception {
        assertRoundTrip("u\"1", "a\\b", "he said \"hi\" \\ end");
    }

    /**
     * Newlines and carriage returns survive.
     *
     * @throws Exception parse failure
     */
    @Test
    void newlineAndCarriageReturnSurvive() throws Exception {
        assertRoundTrip("u", "line1\nline2", "a\r\nb");
    }

    /**
     * A literal tab survives (the original defect regression).
     *
     * @throws Exception parse failure
     */
    @Test
    void tabSurvives() throws Exception {
        assertRoundTrip("u", "t", "col1\tcol2\tcol3");
    }

    /**
     * Other C0 control characters survive.
     *
     * @throws Exception parse failure
     */
    @Test
    void controlCharactersSurvive() throws Exception {
        assertRoundTrip("u", "t", "a\u0008b\u000cc\u001fd");
    }

    /**
     * U+2028 / U+2029 survive (valid JSON; kept literal by Jackson).
     *
     * @throws Exception parse failure
     */
    @Test
    void lineSeparatorSurvives() throws Exception {
        assertRoundTrip("u", "t", "a\u2028b\u2029c");
    }

    /**
     * Chinese and emoji survive.
     *
     * @throws Exception parse failure
     */
    @Test
    void nonAsciiSurvives() throws Exception {
        assertRoundTrip("用户", "标题", "中文与 emoji 😀 混排");
    }

    /**
     * Null values become an empty string.
     *
     * @throws Exception parse failure
     */
    @Test
    void nullValuesBecomeEmptyString() throws Exception {
        Map<String, String> parsed = parse(channel.toJson(cmd(null, null, null)));

        Assertions.assertEquals("", parsed.get("to"));
        Assertions.assertEquals("", parsed.get("title"));
        Assertions.assertEquals("", parsed.get("content"));
    }

    /**
     * A missing endpoint yields a failed result.
     */
    @Test
    void missingEndpointFails() {
        MessageResult nullCommand = channel.send(null);
        MessageResult noUrl = channel.send(new MessageCommand("webhook", "u", "text", Map.of(), null, null, null,
                "t", "c"));

        Assertions.assertFalse(nullCommand.success());
        Assertions.assertFalse(noUrl.success());
    }
}

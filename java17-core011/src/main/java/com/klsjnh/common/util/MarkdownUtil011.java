package com.klsjnh.common.util;

/*                MarkdownUtil011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  markdown utility 011 class
 *
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown table helpers — the single home for markdown table syntax in the
 * framework. Pure Java shared kernel, usable from any layer.
 * <p>
 * Only markdown <b>syntax</b> lives here: cell escape/unescape, row parse,
 * separator detection and table rendering. Callers own their own semantics
 * (sections, column mapping, typing) and MUST NOT hand-roll pipe/escape logic.
 * </p>
 */

public final class MarkdownUtil011 {

    /**
     * Utility: no instances.
     */
    private MarkdownUtil011() {
    }

    /**
     * Escape one table cell: pipes and line breaks would break the row.
     *
     * @param value raw value, nullable
     * @return escaped cell, never null
     */
    public static String escapeCell(String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", " ").replace("\r", " ");
    }

    /**
     * Unescape one table cell (reverse of {@link #escapeCell}).
     *
     * @param value escaped cell, nullable
     * @return raw cell, never null
     */
    public static String unescapeCell(String value) {
        return value == null ? "" : value.replace("\\|", "|");
    }

    /**
     * Whether a line looks like a table row (optionally indented).
     *
     * @param line raw line, nullable
     * @return true when the trimmed line starts with a pipe
     */
    public static boolean isTableRow(String line) {
        return line != null && line.trim().startsWith("|");
    }

    /**
     * Parse a markdown table row into raw cells (outer pipes stripped,
     * {@code \|} unescaped, cells trimmed).
     *
     * @param line raw line
     * @return cells
     */
    public static List<String> parseRow(String line) {
        String body = line == null ? "" : line.trim();

        if (body.startsWith("|")) {
            body = body.substring(1);
        }

        if (body.endsWith("|")) {
            body = body.substring(0, body.length() - 1);
        }

        List<String> cells = new ArrayList<>();

        for (String part : body.split("(?<!\\\\)\\|", -1)) {
            cells.add(unescapeCell(part.trim()));
        }

        return cells;
    }

    /**
     * Whether every cell is a markdown table separator (e.g. {@code ---}).
     *
     * @param cells cells
     * @return true when the row is a separator
     */
    public static boolean isSeparatorRow(List<String> cells) {
        if (cells == null || cells.isEmpty()) {
            return false;
        }

        for (String cell : cells) {
            if (cell == null || !cell.trim().matches(":?-{1,}:?")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Render a markdown table (header, separator, rows). Values are escaped;
     * the returned text ends with a newline.
     *
     * @param headers header cells
     * @param rows    row cells
     * @return markdown table text
     */
    public static String renderTable(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("| ").append(String.join(" | ", escapeAll(headers))).append(" |\n");
        sb.append('|').append(" --- |".repeat(headers.size())).append('\n');

        if (rows != null) {
            for (List<String> row : rows) {
                sb.append("| ").append(String.join(" | ", escapeAll(row))).append(" |\n");
            }
        }

        return sb.toString();
    }

    /**
     * Escape every cell.
     *
     * @param cells cells
     * @return escaped cells
     */
    private static List<String> escapeAll(List<String> cells) {
        List<String> out = new ArrayList<>();

        if (cells != null) {
            for (String cell : cells) {
                out.add(escapeCell(cell));
            }
        }

        return out;
    }
}

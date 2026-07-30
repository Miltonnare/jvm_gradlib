package com.aura.engine;

import com.aura.internal.Preconditions;
import java.util.List;
import java.util.Map;

/**
 * Textual template engine that processes variables, conditionals, and repeating lists.
 */
public class TemplateEngine {
    private static final int MAX_NESTING = 8;

    /**
     * Renders template text replacing placeholders using key-value values.
     */
    public String render(String template, Map<String, Object> context) {
        Preconditions.checkNotNull(template, "Template string cannot be null");
        Preconditions.checkNotNull(context, "Context map cannot be null");
        return renderBlock(template, context, 0);
    }

    private String renderBlock(String block, Map<String, Object> context, int depth) {
        if (depth > MAX_NESTING) {
            throw new IllegalStateException("Exceeded maximum template nesting depth of " + MAX_NESTING);
        }

        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        int len = block.length();

        while (cursor < len) {
            int startIdx = block.indexOf("{{", cursor);
            if (startIdx == -1) {
                sb.append(block.substring(cursor));
                break;
            }

            sb.append(block, cursor, startIdx);
            int endIdx = block.indexOf("}}", startIdx);
            if (endIdx == -1) {
                // Malformed tag, treat as literal text or throw
                throw new IllegalArgumentException("Unclosed template placeholder starting at position " + startIdx);
            }

            String tagContent = block.substring(startIdx + 2, endIdx).trim();
            cursor = endIdx + 2;

            if (tagContent.startsWith("if ")) {
                String varName = tagContent.substring(3).trim();
                int matchingEnd = findMatchingEndTag(block, cursor, "if", "endif");

                String innerBlock = block.substring(cursor, matchingEnd);
                cursor = matchingEnd + 9; // Skip "{{endif}}"

                Object cond = context.get(varName);
                boolean show = cond instanceof Boolean b ? b : cond != null;
                if (show) {
                    sb.append(renderBlock(innerBlock, context, depth + 1));
                }

            } else if (tagContent.startsWith("each ")) {
                String listName = tagContent.substring(5).trim();
                int matchingEnd = findMatchingEndTag(block, cursor, "each", "endeach");

                String innerBlock = block.substring(cursor, matchingEnd);
                cursor = matchingEnd + 11; // Skip "{{endeach}}"

                Object listObj = context.get(listName);
                if (listObj instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> subCtx = (Map<String, Object>) item;
                            sb.append(renderBlock(innerBlock, subCtx, depth + 1));
                        } else {
                            // Flat list item
                            sb.append(item.toString());
                        }
                    }
                }

            } else if ("endif".equals(tagContent) || "endeach".equals(tagContent)) {
                throw new IllegalArgumentException("Orphan end tag: " + tagContent);
            } else {
                // Standard variable replacement
                Object value = context.get(tagContent);
                if (value != null) {
                    sb.append(value.toString());
                }
            }
        }
        return sb.toString();
    }

    private int findMatchingEndTag(String block, int startCursor, String type, String endTag) {
        int depth = 1;
        int cursor = startCursor;
        int len = block.length();

        while (cursor < len) {
            int startIdx = block.indexOf("{{", cursor);
            if (startIdx == -1) {
                break;
            }

            int endIdx = block.indexOf("}}", startIdx);
            if (endIdx == -1) {
                break;
            }

            String tag = block.substring(startIdx + 2, endIdx).trim();
            if (tag.startsWith(type + " ")) {
                depth++;
            } else if (tag.equals(endTag)) {
                depth--;
                if (depth == 0) {
                    return startIdx;
                }
            }
            cursor = endIdx + 2;
        }
        throw new IllegalArgumentException("Missing matching close tag: " + endTag);
    }
}

package com.aura.engine.template;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateful lexer for the AuraTemplate engine.
 * Alternates between TEXT mode and TAG block mode.
 */
public class TemplateLexer {
    private final String src;
    private final int len;
    private int idx;
    private boolean inTag = false;

    public TemplateLexer(String source) {
        this.src = Preconditions.checkNotNull(source, "Template source cannot be null");
        this.len = source.length();
        this.idx = 0;
    }

    public List<TemplateToken> tokenize() {
        List<TemplateToken> tokens = new ArrayList<>();

        while (idx < len) {
            if (!inTag) {
                // TEXT Mode
                int tagStart = src.indexOf("{{", idx);
                if (tagStart == -1) {
                    // Rest of the source is text
                    String text = src.substring(idx);
                    tokens.add(new TemplateToken(TemplateToken.Type.TEXT, text, idx));
                    idx = len;
                } else {
                    if (tagStart > idx) {
                        String text = src.substring(idx, tagStart);
                        tokens.add(new TemplateToken(TemplateToken.Type.TEXT, text, idx));
                    }
                    tokens.add(new TemplateToken(TemplateToken.Type.VAR_START, "{{", tagStart));
                    idx = tagStart + 2;
                    inTag = true;
                }
            } else {
                // TAG Mode
                char c = peek();
                if (Character.isWhitespace(c)) {
                    advance();
                    continue;
                }

                if (c == '}' && peek(1) == '}') {
                    tokens.add(new TemplateToken(TemplateToken.Type.VAR_END, "}}", idx));
                    idx += 2;
                    inTag = false;
                    continue;
                }

                if (c == '.') {
                    tokens.add(new TemplateToken(TemplateToken.Type.DOT, ".", idx));
                    advance();
                    continue;
                }

                if (c == '|') {
                    tokens.add(new TemplateToken(TemplateToken.Type.PIPE, "|", idx));
                    advance();
                    continue;
                }

                if (c == '"' || c == '\'') {
                    tokens.add(readString(c));
                    continue;
                }

                if (isIdentifierStart(c)) {
                    tokens.add(readIdentifier());
                    continue;
                }

                throw new IllegalArgumentException("Invalid character in template tag: '" + c + "' at position " + idx);
            }
        }

        tokens.add(new TemplateToken(TemplateToken.Type.EOF, "", idx));
        return tokens;
    }

    private char peek() {
        if (idx >= len) return '\0';
        return src.charAt(idx);
    }

    private char peek(int offset) {
        if (idx + offset >= len) return '\0';
        return src.charAt(idx + offset);
    }

    private void advance() {
        if (idx < len) {
            idx++;
        }
    }

    private TemplateToken readString(char quote) {
        int pos = idx;
        advance(); // consume quote
        StringBuilder sb = new StringBuilder();
        while (idx < len && peek() != quote) {
            sb.append(peek());
            advance();
        }
        if (idx >= len) {
            throw new IllegalArgumentException("Unclosed string literal in template tag at position " + pos);
        }
        advance(); // consume close quote
        return new TemplateToken(TemplateToken.Type.STRING, sb.toString(), pos);
    }

    private TemplateToken readIdentifier() {
        int pos = idx;
        StringBuilder sb = new StringBuilder();
        while (idx < len && isIdentifierPart(peek())) {
            sb.append(peek());
            advance();
        }
        String val = sb.toString();
        TemplateToken.Type type = switch (val) {
            case "if" -> TemplateToken.Type.IF;
            case "else" -> TemplateToken.Type.ELSE;
            case "endif" -> TemplateToken.Type.ENDIF;
            case "for" -> TemplateToken.Type.FOR;
            case "in" -> TemplateToken.Type.IN;
            case "endfor" -> TemplateToken.Type.ENDFOR;
            default -> TemplateToken.Type.IDENTIFIER;
        };
        return new TemplateToken(type, val, pos);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}

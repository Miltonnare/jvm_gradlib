package com.aura.model.query;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for the AuraQuery expression language.
 */
public class QueryLexer {
    private final String src;
    private final int len;
    private int idx;

    public QueryLexer(String source) {
        this.src = Preconditions.checkNotNull(source, "Source query cannot be null");
        this.len = source.length();
        this.idx = 0;
    }

    public List<QueryToken> tokenize() {
        List<QueryToken> tokens = new ArrayList<>();
        while (idx < len) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            if (c == '/') {
                int pos = idx;
                advance();
                if (peek() == '/') {
                    advance();
                    tokens.add(new QueryToken(QueryToken.Type.DOUBLE_SLASH, "//", pos));
                } else {
                    tokens.add(new QueryToken(QueryToken.Type.SLASH, "/", pos));
                }
                continue;
            }

            if (c == '*') {
                tokens.add(new QueryToken(QueryToken.Type.STAR, "*", idx));
                advance();
                continue;
            }

            if (c == '[') {
                tokens.add(new QueryToken(QueryToken.Type.LBRACKET, "[", idx));
                advance();
                continue;
            }

            if (c == ']') {
                tokens.add(new QueryToken(QueryToken.Type.RBRACKET, "]", idx));
                advance();
                continue;
            }

            if (c == '@') {
                tokens.add(new QueryToken(QueryToken.Type.AT, "@", idx));
                advance();
                continue;
            }

            if (c == '=') {
                tokens.add(new QueryToken(QueryToken.Type.EQUALS, "=", idx));
                advance();
                continue;
            }

            if (c == '!' && peek(1) == '=') {
                int pos = idx;
                advance();
                advance();
                tokens.add(new QueryToken(QueryToken.Type.NOT_EQUALS, "!=", pos));
                continue;
            }

            if (c == '"' || c == '\'') {
                tokens.add(readStringLiteral(c));
                continue;
            }

            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            if (isIdentifierStart(c)) {
                tokens.add(readIdentifier());
                continue;
            }

            throw new IllegalArgumentException(String.format("Invalid character '%s' in query expression at position %d", c, idx));
        }

        tokens.add(new QueryToken(QueryToken.Type.EOF, "", idx));
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

    private QueryToken readStringLiteral(char quote) {
        int pos = idx;
        advance(); // consume quote
        StringBuilder sb = new StringBuilder();
        while (idx < len && peek() != quote) {
            sb.append(peek());
            advance();
        }
        if (idx >= len) {
            throw new IllegalArgumentException("Unclosed string literal in query at position " + pos);
        }
        advance(); // consume close quote
        return new QueryToken(QueryToken.Type.STRING, sb.toString(), pos);
    }

    private QueryToken readNumber() {
        int pos = idx;
        StringBuilder sb = new StringBuilder();
        while (idx < len && Character.isDigit(peek())) {
            sb.append(peek());
            advance();
        }
        return new QueryToken(QueryToken.Type.NUMBER, sb.toString(), pos);
    }

    private QueryToken readIdentifier() {
        int pos = idx;
        StringBuilder sb = new StringBuilder();
        while (idx < len && isIdentifierPart(peek())) {
            sb.append(peek());
            advance();
        }
        return new QueryToken(QueryToken.Type.IDENTIFIER, sb.toString(), pos);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '$';
    }
}

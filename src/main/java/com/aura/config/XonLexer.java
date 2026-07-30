package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for tokenizing Xon Config Format (.xcf).
 */
public class XonLexer {
    private final String src;
    private final int len;
    private int idx;
    private int line;
    private int col;

    public XonLexer(String source) {
        this.src = Preconditions.checkNotNull(source, "Source configuration cannot be null");
        this.len = source.length();
        this.idx = 0;
        this.line = 1;
        this.col = 1;
    }

    public List<XonToken> tokenize() {
        List<XonToken> tokens = new ArrayList<>();
        while (idx < len) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                consumeWhitespace();
                continue;
            }

            // Comments
            if (c == '#' || (c == '/' && peek(1) == '/')) {
                consumeComment();
                continue;
            }

            // Symbols
            if (c == '{') {
                tokens.add(new XonToken(XonToken.Type.LBRACE, "{", line, col));
                advance();
                continue;
            }
            if (c == '}') {
                tokens.add(new XonToken(XonToken.Type.RBRACE, "}", line, col));
                advance();
                continue;
            }
            if (c == '[') {
                tokens.add(new XonToken(XonToken.Type.LBRACKET, "[", line, col));
                advance();
                continue;
            }
            if (c == ']') {
                tokens.add(new XonToken(XonToken.Type.RBRACKET, "]", line, col));
                advance();
                continue;
            }
            if (c == ':') {
                tokens.add(new XonToken(XonToken.Type.COLON, ":", line, col));
                advance();
                continue;
            }
            if (c == ',') {
                tokens.add(new XonToken(XonToken.Type.COMMA, ",", line, col));
                advance();
                continue;
            }

            // Strings (Quoted)
            if (c == '"' || c == '\'') {
                tokens.add(readStringLiteral(c));
                continue;
            }

            // Numbers or Identifiers/Keywords
            if (Character.isDigit(c) || c == '-' || c == '+') {
                tokens.add(readNumber());
                continue;
            }

            if (isIdentifierStart(c)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            throw new IllegalArgumentException(String.format("Unexpected character '%s' at line %d, col %d", c, line, col));
        }

        tokens.add(new XonToken(XonToken.Type.EOF, "", line, col));
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
            char c = src.charAt(idx);
            idx++;
            if (c == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
    }

    private void consumeWhitespace() {
        while (idx < len && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private void consumeComment() {
        // Read until end of line
        while (idx < len && peek() != '\n' && peek() != '\r') {
            advance();
        }
    }

    private XonToken readStringLiteral(char quote) {
        int startLine = line;
        int startCol = col;
        advance(); // Consume open quote

        StringBuilder sb = new StringBuilder();
        while (idx < len && peek() != quote) {
            char c = peek();
            if (c == '\\') {
                advance();
                char esc = peek();
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case '"' -> sb.append('"');
                    case '\'' -> sb.append('\'');
                    default -> throw new IllegalArgumentException("Unknown escape character: \\" + esc + " at " + line + ":" + col);
                }
            } else {
                sb.append(c);
            }
            advance();
        }

        if (idx >= len) {
            throw new IllegalArgumentException("Unclosed string literal starting at " + startLine + ":" + startCol);
        }
        advance(); // Consume close quote

        return new XonToken(XonToken.Type.STRING, sb.toString(), startLine, startCol);
    }

    private XonToken readNumber() {
        int startLine = line;
        int startCol = col;
        StringBuilder sb = new StringBuilder();

        // Sign
        if (peek() == '-' || peek() == '+') {
            sb.append(peek());
            advance();
        }

        boolean hasDecimals = false;
        boolean hasExponent = false;

        while (idx < len) {
            char c = peek();
            if (Character.isDigit(c)) {
                sb.append(c);
                advance();
            } else if (c == '.' && !hasDecimals && Character.isDigit(peek(1))) {
                hasDecimals = true;
                sb.append(c);
                advance();
            } else if ((c == 'e' || c == 'E') && !hasExponent) {
                hasExponent = true;
                sb.append(c);
                advance();
                if (peek() == '+' || peek() == '-') {
                    sb.append(peek());
                    advance();
                }
            } else {
                break;
            }
        }

        return new XonToken(XonToken.Type.NUMBER, sb.toString(), startLine, startCol);
    }

    private XonToken readIdentifierOrKeyword() {
        int startLine = line;
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        while (idx < len && isIdentifierPart(peek())) {
            sb.append(peek());
            advance();
        }

        String word = sb.toString();
        if ("true".equalsIgnoreCase(word) || "false".equalsIgnoreCase(word)) {
            return new XonToken(XonToken.Type.BOOLEAN, word.toLowerCase(), startLine, startCol);
        }
        if ("null".equalsIgnoreCase(word)) {
            return new XonToken(XonToken.Type.NULL, "null", startLine, startCol);
        }
        return new XonToken(XonToken.Type.IDENTIFIER, word, startLine, startCol);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$' || c == '-';
    }
}

package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.List;

/**
 * Recursive descent LL(1) parser for Xon Config Format.
 */
public class XonParser {
    private static final int MAX_RECURSION_DEPTH = 64;
    private final List<XonToken> tokens;
    private int idx;

    public XonParser(List<XonToken> tokens) {
        this.tokens = Preconditions.checkNotNull(tokens, "Tokens list cannot be null");
        this.idx = 0;
    }

    public XonValue parse() {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No tokens to parse");
        }
        XonValue val = parseValue(0);
        consume(XonToken.Type.EOF, "Expected EOF at end of configuration");
        return val;
    }

    private XonValue parseValue(int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            throw new IllegalStateException("Maximum configuration nesting depth exceeded");
        }

        XonToken token = peek();
        switch (token.getType()) {
            case LBRACE -> {
                return parseObject(depth);
            }
            case LBRACKET -> {
                return parseArray(depth);
            }
            case STRING -> {
                consume();
                return new XonPrimitive(token.getValue());
            }
            case NUMBER -> {
                consume();
                String raw = token.getValue();
                try {
                    if (raw.indexOf('.') >= 0 || raw.indexOf('e') >= 0 || raw.indexOf('E') >= 0) {
                        return new XonPrimitive(Double.parseDouble(raw));
                    } else {
                        // Fit in integer or long
                        long val = Long.parseLong(raw);
                        if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                            return new XonPrimitive((int) val);
                        }
                        return new XonPrimitive(val);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Malformed number '%s' at %d:%d", raw, token.getLine(), token.getColumn()), e);
                }
            }
            case BOOLEAN -> {
                consume();
                return new XonPrimitive(Boolean.parseBoolean(token.getValue()));
            }
            case NULL -> {
                consume();
                return new XonPrimitive(null);
            }
            case IDENTIFIER -> {
                // Treats unquoted identifier as string
                consume();
                return new XonPrimitive(token.getValue());
            }
            default -> throw new IllegalArgumentException(
                String.format("Expected value expression but got token %s at line %d, column %d",
                    token.getType(), token.getLine(), token.getColumn())
            );
        }
    }

    private XonObject parseObject(int depth) {
        consume(XonToken.Type.LBRACE, "Expected '{'");
        XonObject obj = new XonObject();

        while (peek().getType() != XonToken.Type.RBRACE) {
            XonToken keyToken = peek();
            if (keyToken.getType() != XonToken.Type.STRING && keyToken.getType() != XonToken.Type.IDENTIFIER) {
                throw new IllegalArgumentException(
                    String.format("Expected key string or identifier but got token %s at line %d, column %d",
                        keyToken.getType(), keyToken.getLine(), keyToken.getColumn())
                );
            }
            consume();
            String key = keyToken.getValue();

            consume(XonToken.Type.COLON, "Expected ':' key-value separator");
            XonValue val = parseValue(depth + 1);
            obj.put(key, val);

            // Match optional comma separator
            if (peek().getType() == XonToken.Type.COMMA) {
                consume();
                // Trailing commas allowed, if next is RBRACE we break
                if (peek().getType() == XonToken.Type.RBRACE) {
                    break;
                }
            } else if (peek().getType() != XonToken.Type.RBRACE && peek().getType() != XonToken.Type.EOF) {
                // Commas are technically optional, but need bounds checking
                // If it's not a comma or brace, it must be the start of a new field key
                if (peek().getType() != XonToken.Type.STRING && peek().getType() != XonToken.Type.IDENTIFIER) {
                    throw new IllegalArgumentException(
                        String.format("Missing separator or key between fields, got %s at %d:%d",
                            peek().getType(), peek().getLine(), peek().getColumn())
                    );
                }
            }
        }

        consume(XonToken.Type.RBRACE, "Expected '}'");
        return obj;
    }

    private XonArray parseArray(int depth) {
        consume(XonToken.Type.LBRACKET, "Expected '['");
        XonArray arr = new XonArray();

        while (peek().getType() != XonToken.Type.RBRACKET) {
            XonValue val = parseValue(depth + 1);
            arr.add(val);

            // Optional comma separator
            if (peek().getType() == XonToken.Type.COMMA) {
                consume();
                if (peek().getType() == XonToken.Type.RBRACKET) {
                    break; // Trailing comma allowed
                }
            } else if (peek().getType() != XonToken.Type.RBRACKET && peek().getType() != XonToken.Type.EOF) {
                // If there's no comma, the next token must start a valid value
                XonToken.Type t = peek().getType();
                if (t != XonToken.Type.LBRACE && t != XonToken.Type.LBRACKET && t != XonToken.Type.STRING
                    && t != XonToken.Type.NUMBER && t != XonToken.Type.BOOLEAN && t != XonToken.Type.NULL
                    && t != XonToken.Type.IDENTIFIER) {
                    throw new IllegalArgumentException(
                        String.format("Missing comma or array closing bracket, got %s at %d:%d",
                            t, peek().getLine(), peek().getColumn())
                    );
                }
            }
        }

        consume(XonToken.Type.RBRACKET, "Expected ']'");
        return arr;
    }

    private XonToken peek() {
        if (idx >= tokens.size()) {
            return new XonToken(XonToken.Type.EOF, "", 0, 0);
        }
        return tokens.get(idx);
    }

    private void consume() {
        if (idx < tokens.size()) {
            idx++;
        }
    }

    private void consume(XonToken.Type expectedType, String errorMsg) {
        XonToken current = peek();
        if (current.getType() != expectedType) {
            throw new IllegalArgumentException(
                String.format("%s. Got token %s at line %d, column %d", errorMsg, current.getType(), current.getLine(), current.getColumn())
            );
        }
        consume();
    }
}

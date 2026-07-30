package com.aura.config;

/**
 * Represents a Token in the Xon Config Language (.xcf).
 */
public class XonToken {
    public enum Type {
        LBRACE, RBRACE,
        LBRACKET, RBRACKET,
        COLON, COMMA,
        STRING, NUMBER, BOOLEAN, NULL,
        IDENTIFIER,
        EOF
    }

    private final Type type;
    private final String value;
    private final int line;
    private final int column;

    public XonToken(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public Type getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return String.format("%s('%s')@%d:%d", type, value, line, column);
    }
}

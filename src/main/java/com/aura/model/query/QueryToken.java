package com.aura.model.query;

/**
 * Token representations for the AuraQuery expression language.
 */
public class QueryToken {
    public enum Type {
        SLASH,         // '/'
        DOUBLE_SLASH,  // '//'
        STAR,          // '*'
        LBRACKET,      // '['
        RBRACKET,      // ']'
        AT,            // '@'
        EQUALS,        // '='
        NOT_EQUALS,    // '!='
        STRING,        // '"value"' or "'value'"
        NUMBER,        // '123'
        IDENTIFIER,    // 'paragraph' or 'class'
        EOF
    }

    private final Type type;
    private final String value;
    private final int position;

    public QueryToken(Type type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public Type getType() { return type; }
    public String getValue() { return value; }
    public int getPosition() { return position; }

    @Override
    public String toString() {
        return String.format("%s('%s')@%d", type, value, position);
    }
}

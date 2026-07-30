package com.aura.engine.template;

/**
 * Token representations for the AuraTemplate engine.
 */
public class TemplateToken {
    public enum Type {
        TEXT,
        VAR_START,    // '{{'
        VAR_END,      // '}}'
        IDENTIFIER,   // 'user', 'name', 'item', 'items'
        DOT,          // '.'
        PIPE,         // '|'
        IF,           // 'if'
        ELSE,         // 'else'
        ENDIF,        // 'endif'
        FOR,          // 'for'
        IN,           // 'in'
        ENDFOR,       // 'endfor'
        STRING,       // '"value"' or "'value'"
        EOF
    }

    private final Type type;
    private final String value;
    private final int position;

    public TemplateToken(Type type, String value, int position) {
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

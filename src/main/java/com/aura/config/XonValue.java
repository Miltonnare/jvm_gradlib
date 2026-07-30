package com.aura.config;

import java.util.List;
import java.util.Map;

/**
 * Base AST node class for Xon Config elements.
 */
public abstract class XonValue {

    public boolean isObject() { return false; }
    public boolean isArray() { return false; }
    public boolean isPrimitive() { return false; }

    public Map<String, XonValue> asObject() {
        throw new UnsupportedOperationException("Value is not a XonObject");
    }

    public List<XonValue> asArray() {
        throw new UnsupportedOperationException("Value is not a XonArray");
    }

    public String asString() {
        throw new UnsupportedOperationException("Value is not a string primitive");
    }

    public double asDouble() {
        throw new UnsupportedOperationException("Value is not a number primitive");
    }

    public int asInt() {
        throw new UnsupportedOperationException("Value is not a number primitive");
    }

    public boolean asBoolean() {
        throw new UnsupportedOperationException("Value is not a boolean primitive");
    }

    public boolean isNull() { return false; }

    @Override
    public abstract String toString();
}

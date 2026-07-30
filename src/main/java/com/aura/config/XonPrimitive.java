package com.aura.config;

import com.aura.internal.Preconditions;

/**
 * Node for primitive Xon values (Strings, Numbers, Booleans, Null).
 */
public class XonPrimitive extends XonValue {
    private final Object val;

    public XonPrimitive(Object val) {
        if (val != null) {
            Preconditions.checkArgument(
                val instanceof String || val instanceof Number || val instanceof Boolean,
                "Unsupported primitive type: " + val.getClass()
            );
        }
        this.val = val;
    }

    @Override
    public boolean isPrimitive() { return true; }

    @Override
    public boolean isNull() { return val == null; }

    @Override
    public String asString() {
        return val == null ? "null" : val.toString();
    }

    @Override
    public double asDouble() {
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        throw new UnsupportedOperationException("Value is not a number: " + val);
    }

    @Override
    public int asInt() {
        if (val instanceof Number num) {
            return num.intValue();
        }
        throw new UnsupportedOperationException("Value is not a number: " + val);
    }

    @Override
    public boolean asBoolean() {
        if (val instanceof Boolean b) {
            return b;
        }
        throw new UnsupportedOperationException("Value is not a boolean: " + val);
    }

    @Override
    public String toString() {
        if (val == null) {
            return "null";
        }
        if (val instanceof String) {
            // Basic escape replacement for representation
            return "\"" + val.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
        }
        return val.toString();
    }
}

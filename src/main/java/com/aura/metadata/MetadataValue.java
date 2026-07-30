package com.aura.metadata;

import com.aura.internal.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holder for a typed metadata value.
 */
public abstract class MetadataValue {

    public abstract MetadataType getType();

    public int asInt() {
        throw new UnsupportedOperationException("Not an integer");
    }

    public long asLong() {
        throw new UnsupportedOperationException("Not a long");
    }

    public double asDouble() {
        throw new UnsupportedOperationException("Not a double");
    }

    public boolean asBoolean() {
        throw new UnsupportedOperationException("Not a boolean");
    }

    public String asString() {
        throw new UnsupportedOperationException("Not a string");
    }

    public byte[] asBinary() {
        throw new UnsupportedOperationException("Not binary data");
    }

    public Map<String, MetadataValue> asMap() {
        throw new UnsupportedOperationException("Not a map");
    }

    public List<MetadataValue> asArray() {
        throw new UnsupportedOperationException("Not an array");
    }

    // Static constructors

    public static MetadataValue of(int value) {
        return new IntVal(value);
    }

    public static MetadataValue of(long value) {
        return new LongVal(value);
    }

    public static MetadataValue of(double value) {
        return new DoubleVal(value);
    }

    public static MetadataValue of(boolean value) {
        return new BoolVal(value);
    }

    public static MetadataValue of(String value) {
        return new StringVal(value);
    }

    public static MetadataValue of(byte[] value) {
        return new BinaryVal(value);
    }

    public static MetadataValue of(Map<String, MetadataValue> value) {
        return new MapVal(value);
    }

    public static MetadataValue of(List<MetadataValue> value) {
        return new ArrayVal(value);
    }

    // Concrete Subclasses

    private static final class IntVal extends MetadataValue {
        private final int val;

        private IntVal(int val) { this.val = val; }

        @Override public MetadataType getType() { return MetadataType.INTEGER; }
        @Override public int asInt() { return val; }
        @Override public long asLong() { return val; }
        @Override public double asDouble() { return val; }
        @Override public String toString() { return String.valueOf(val); }
    }

    private static final class LongVal extends MetadataValue {
        private final long val;

        private LongVal(long val) { this.val = val; }

        @Override public MetadataType getType() { return MetadataType.LONG; }
        @Override public long asLong() { return val; }
        @Override public double asDouble() { return val; }
        @Override public String toString() { return String.valueOf(val); }
    }

    private static final class DoubleVal extends MetadataValue {
        private final double val;

        private DoubleVal(double val) { this.val = val; }

        @Override public MetadataType getType() { return MetadataType.DOUBLE; }
        @Override public double asDouble() { return val; }
        @Override public String toString() { return String.valueOf(val); }
    }

    private static final class BoolVal extends MetadataValue {
        private final boolean val;

        private BoolVal(boolean val) { this.val = val; }

        @Override public MetadataType getType() { return MetadataType.BOOLEAN; }
        @Override public boolean asBoolean() { return val; }
        @Override public String toString() { return String.valueOf(val); }
    }

    private static final class StringVal extends MetadataValue {
        private final String val;

        private StringVal(String val) {
            this.val = Preconditions.checkNotNull(val, "String value cannot be null");
        }

        @Override public MetadataType getType() { return MetadataType.STRING; }
        @Override public String asString() { return val; }
        @Override public String toString() { return val; }
    }

    private static final class BinaryVal extends MetadataValue {
        private final byte[] val;

        private BinaryVal(byte[] val) {
            this.val = Preconditions.checkNotNull(val, "Binary value cannot be null").clone();
        }

        @Override public MetadataType getType() { return MetadataType.BINARY; }
        @Override public byte[] asBinary() { return val.clone(); }
        @Override public String toString() { return "binary[" + val.length + " bytes]"; }
    }

    private static final class MapVal extends MetadataValue {
        private final Map<String, MetadataValue> val;

        private MapVal(Map<String, MetadataValue> val) {
            Preconditions.checkNotNull(val, "Map value cannot be null");
            this.val = Collections.unmodifiableMap(val);
        }

        @Override public MetadataType getType() { return MetadataType.MAP; }
        @Override public Map<String, MetadataValue> asMap() { return val; }
        @Override public String toString() { return val.toString(); }
    }

    private static final class ArrayVal extends MetadataValue {
        private final List<MetadataValue> val;

        private ArrayVal(List<MetadataValue> val) {
            Preconditions.checkNotNull(val, "Array value cannot be null");
            this.val = List.copyOf(val);
        }

        @Override public MetadataType getType() { return MetadataType.ARRAY; }
        @Override public List<MetadataValue> asArray() { return val; }
        @Override public String toString() { return val.toString(); }
    }
}

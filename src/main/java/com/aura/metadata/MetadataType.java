package com.aura.metadata;

/**
 * Enumeration of supported metadata value types in Binary Structured Metadata.
 */
public enum MetadataType {
    INTEGER((byte) 0x01),
    LONG((byte) 0x02),
    DOUBLE((byte) 0x03),
    BOOLEAN((byte) 0x04),
    STRING((byte) 0x05),
    BINARY((byte) 0x06),
    MAP((byte) 0x07),
    ARRAY((byte) 0x08);

    private final byte code;

    MetadataType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static MetadataType fromCode(byte code) {
        for (MetadataType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown metadata type code: " + code);
    }
}

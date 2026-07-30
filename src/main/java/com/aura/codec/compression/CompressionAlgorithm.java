package com.aura.codec.compression;

/**
 * Supported compression algorithms in the Aura Container format.
 */
public enum CompressionAlgorithm {
    NONE((byte) 0),
    RLE((byte) 1),
    LZ77((byte) 2);

    private final byte code;

    CompressionAlgorithm(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static CompressionAlgorithm fromCode(byte code) {
        for (CompressionAlgorithm algorithm : values()) {
            if (algorithm.code == code) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Unknown compression code: " + code);
    }
}

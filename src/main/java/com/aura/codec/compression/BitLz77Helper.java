package com.aura.codec.compression;

import com.aura.internal.Preconditions;

/**
 * Calculations helper for sliding window offsets and bit allocations in LZ77.
 */
public final class BitLz77Helper {

    private BitLz77Helper() {
        // Prevent instantiation
    }

    /**
     * Asserts if match parameters satisfy sliding window sizes and bounds.
     */
    public static void validateBounds(int offset, int length, int windowSize, int maxMatchLen) {
        Preconditions.checkArgument(windowSize > 0, "Window size must be positive");
        Preconditions.checkArgument(maxMatchLen > 0, "Max match length must be positive");
        Preconditions.checkArgument(offset > 0 && offset <= windowSize, "Offset exceeds sliding window boundary");
        Preconditions.checkArgument(length >= 3 && length <= maxMatchLen, "Length is outside limits");
    }

    /**
     * Checks if a character matches the byte search pattern.
     */
    public static boolean checkEquality(byte[] data, int i, int cursor, int offset, int windowSize) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        Preconditions.checkArgument(windowSize > 0, "Window size must be positive");
        int index = i + offset;
        if (index < 0 || index >= data.length) {
            return false;
        }
        return data[index] == data[cursor + offset];
    }
}

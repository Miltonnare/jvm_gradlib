package com.aura.codec.compression;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import com.aura.stream.BitInputStream;
import com.aura.stream.BitOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of bit-packed LZ77 compression and decompression.
 * Prefixes the compressed stream with a 4-byte uncompressed length header.
 * Uses a sliding window size of 1024 bytes and a lookahead buffer of 18 bytes.
 */
public final class Lz77Compression {
    private static final int WINDOW_SIZE = 1024;
    private static final int MAX_MATCH_LEN = 18;
    private static final int MIN_MATCH_LEN = 3;

    private Lz77Compression() {
        // Prevent instantiation
    }

    /**
     * Compresses the input data using LZ77 sliding window.
     */
    public static byte[] compress(byte[] data) throws IOException {
        Preconditions.checkNotNull(data, "Data cannot be null");
        if (data.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Prepend uncompressed length (4 bytes)
        byte[] lenBytes = new byte[4];
        ByteUtils.writeIntBe(lenBytes, 0, data.length);
        baos.write(lenBytes);

        try (BitOutputStream bos = new BitOutputStream(baos)) {
            int cursor = 0;
            while (cursor < data.length) {
                int matchLength = 0;
                int matchOffset = 0;

                // Find longest match in the sliding window
                int windowStart = Math.max(0, cursor - WINDOW_SIZE);
                for (int i = windowStart; i < cursor; i++) {
                    int len = 0;
                    while (len < MAX_MATCH_LEN && cursor + len < data.length 
                           && data[i + len] == data[cursor + len]) {
                        len++;
                    }

                    if (len > matchLength) {
                        matchLength = len;
                        matchOffset = cursor - i;
                    }
                }

                if (matchLength >= MIN_MATCH_LEN) {
                    // Write back-reference token: bit 1
                    bos.writeBit(1);
                    // Offset: 10 bits (WINDOW_SIZE = 1024)
                    // Offset value is 1-indexed (1 to 1024)
                    bos.writeBits(matchOffset - 1, 10);
                    // Length: 4 bits (MIN_MATCH_LEN = 3, value range 0 to 15 maps to length 3 to 18)
                    bos.writeBits(matchLength - MIN_MATCH_LEN, 4);
                    cursor += matchLength;
                } else {
                    // Write literal token: bit 0 followed by 8-bit literal byte
                    bos.writeBit(0);
                    bos.writeBits(data[cursor] & 0xFF, 8);
                    cursor++;
                }
            }
            bos.flushBits();
        }
        return baos.toByteArray();
    }

    /**
     * Decompresses the data.
     */
    public static byte[] decompress(byte[] compressed, int maxUncompressedSize) throws IOException {
        Preconditions.checkNotNull(compressed, "Compressed data cannot be null");
        Preconditions.checkArgument(maxUncompressedSize >= 0, "Max size must be non-negative");
        if (compressed.length == 0) {
            return new byte[0];
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        byte[] lenBytes = new byte[4];
        if (bais.read(lenBytes) < 4) {
            throw new IllegalArgumentException("Truncated LZ77 stream header");
        }
        int expectedLength = ByteUtils.readIntBe(lenBytes, 0);
        if (expectedLength < 0 || expectedLength > maxUncompressedSize) {
            throw new IllegalStateException("Invalid LZ77 expected length: " + expectedLength);
        }
        if (expectedLength == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] history = new byte[WINDOW_SIZE];
        int historyCursor = 0;
        int totalUncompressed = 0;

        try (BitInputStream bis = new BitInputStream(bais)) {
            while (totalUncompressed < expectedLength) {
                int tokenType = bis.readBit();
                if (tokenType == -1) {
                    throw new IllegalArgumentException("Truncated LZ77 stream before reaching expected length");
                }

                if (tokenType == 1) {
                    // Back-reference: read offset (10 bits) and length (4 bits)
                    int offsetBits = bis.readBits(10);
                    if (offsetBits == -1) {
                        throw new IllegalArgumentException("Truncated LZ77 offset bits");
                    }
                    int lengthBits = bis.readBits(4);
                    if (lengthBits == -1) {
                        throw new IllegalArgumentException("Truncated LZ77 length bits");
                    }

                    int matchOffset = offsetBits + 1;
                    int matchLength = lengthBits + MIN_MATCH_LEN;

                    if (totalUncompressed + matchLength > expectedLength) {
                        throw new IllegalArgumentException("LZ77 match overflows expected length");
                    }

                    // Reconstruct from sliding history
                    for (int j = 0; j < matchLength; j++) {
                        int index = totalUncompressed - matchOffset;
                        if (index < 0) {
                            throw new IllegalArgumentException("Invalid LZ77 back-reference offset: " + matchOffset);
                        }

                        byte val = history[(historyCursor - matchOffset + WINDOW_SIZE + j) % WINDOW_SIZE];
                        baos.write(val);

                        // Update history
                        history[(historyCursor + j) % WINDOW_SIZE] = val;
                    }
                    historyCursor = (historyCursor + matchLength) % WINDOW_SIZE;
                    totalUncompressed += matchLength;

                } else {
                    // Literal: read 8 bits
                    int literal = bis.readBits(8);
                    if (literal == -1) {
                        throw new IllegalArgumentException("Truncated LZ77 literal byte");
                    }

                    baos.write(literal);
                    history[historyCursor] = (byte) literal;
                    historyCursor = (historyCursor + 1) % WINDOW_SIZE;
                    totalUncompressed++;
                }
            }
        }
        return baos.toByteArray();
    }
}

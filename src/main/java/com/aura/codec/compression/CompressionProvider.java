package com.aura.codec.compression;

import java.io.IOException;

/**
 * Dispatcher for compression and decompression algorithms.
 */
public final class CompressionProvider {

    private CompressionProvider() {
        // Prevent instantiation
    }

    /**
     * Compress data using the specified algorithm.
     */
    public static byte[] compress(byte[] data, CompressionAlgorithm algorithm) {
        if (data == null) {
            return null;
        }
        try {
            return switch (algorithm) {
                case NONE -> data;
                case RLE -> RleCompression.compress(data);
                case LZ77 -> Lz77Compression.compress(data);
            };
        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        }
    }

    /**
     * Decompress data using the specified algorithm and limit bound.
     */
    public static byte[] decompress(byte[] compressed, CompressionAlgorithm algorithm, int maxUncompressedSize) {
        if (compressed == null) {
            return null;
        }
        try {
            return switch (algorithm) {
                case NONE -> {
                    if (compressed.length > maxUncompressedSize) {
                        throw new IllegalStateException("Data exceeds uncompressed limit");
                    }
                    yield compressed;
                }
                case RLE -> RleCompression.decompress(compressed, maxUncompressedSize);
                case LZ77 -> Lz77Compression.decompress(compressed, maxUncompressedSize);
            };
        } catch (IOException e) {
            throw new RuntimeException("Decompression failed due to format error", e);
        }
    }
}

package com.aura.codec.compression;

import com.aura.internal.Preconditions;

/**
 * Tracks performance statistics, compression ratios, and byte entropy metrics.
 */
public class CompressionStats {
    private final int originalSize;
    private final int compressedSize;
    private final long durationNs;

    public CompressionStats(int originalSize, int compressedSize, long durationNs) {
        Preconditions.checkArgument(originalSize >= 0, "Original size must be non-negative");
        Preconditions.checkArgument(compressedSize >= 0, "Compressed size must be non-negative");
        Preconditions.checkArgument(durationNs >= 0, "Duration must be non-negative");
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.durationNs = durationNs;
    }

    public double getCompressionRatio() {
        if (originalSize == 0) return 1.0;
        return (double) compressedSize / originalSize;
    }

    public double getSavingsRatio() {
        if (originalSize == 0) return 0.0;
        return 1.0 - getCompressionRatio();
    }

    public double getThroughputMbPerSec() {
        if (durationNs == 0) return 0.0;
        double mb = (double) originalSize / (1024 * 1024);
        double sec = (double) durationNs / 1_000_000_000.0;
        return mb / sec;
    }

    /**
     * Computes the Shannon entropy of a byte payload as a metric of compressibility.
     */
    public static double computeEntropy(byte[] data) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        if (data.length == 0) return 0.0;

        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[b & 0xFF]++;
        }

        double entropy = 0.0;
        for (int f : frequencies) {
            if (f > 0) {
                double p = (double) f / data.length;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }

    public int getOriginalSize() { return originalSize; }
    public int getCompressedSize() { return compressedSize; }
    public long getDurationNs() { return durationNs; }
}

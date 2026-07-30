package com.aura.internal;

/**
 * Custom CRC32 implementation using a lookup table.
 */
public final class Crc32 {
    private static final int POLYNOMIAL = 0xEDB88320;
    private static final int[] TABLE = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int entry = i;
            for (int j = 0; j < 8; j++) {
                if ((entry & 1) == 1) {
                    entry = (entry >>> 1) ^ POLYNOMIAL;
                } else {
                    entry = entry >>> 1;
                }
            }
            TABLE[i] = entry;
        }
    }

    private int crc = 0xFFFFFFFF;

    public void update(byte[] b, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        for (int i = 0; i < len; i++) {
            int index = (crc ^ b[off + i]) & 0xFF;
            crc = (crc >>> 8) ^ TABLE[index];
        }
    }

    public void update(int b) {
        int index = (crc ^ b) & 0xFF;
        crc = (crc >>> 8) ^ TABLE[index];
    }

    public long getValue() {
        return ((long) ~crc) & 0xFFFFFFFFL;
    }

    public void reset() {
        crc = 0xFFFFFFFF;
    }

    public static long compute(byte[] data) {
        Crc32 crc32 = new Crc32();
        crc32.update(data, 0, data.length);
        return crc32.getValue();
    }

    public static long compute(byte[] data, int off, int len) {
        Crc32 crc32 = new Crc32();
        crc32.update(data, off, len);
        return crc32.getValue();
    }
}

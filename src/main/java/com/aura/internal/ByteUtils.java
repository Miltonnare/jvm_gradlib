package com.aura.internal;

/**
 * Binary and byte manipulation utilities.
 */
public final class ByteUtils {

    private ByteUtils() {
        // Prevent instantiation
    }

    public static int readIntBe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 4, data.length);
        return ((data[offset] & 0xFF) << 24)
             | ((data[offset + 1] & 0xFF) << 16)
             | ((data[offset + 2] & 0xFF) << 8)
             | (data[offset + 3] & 0xFF);
    }

    public static int readIntLe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 4, data.length);
        return (data[offset] & 0xFF)
             | ((data[offset + 1] & 0xFF) << 8)
             | ((data[offset + 2] & 0xFF) << 16)
             | ((data[offset + 3] & 0xFF) << 24);
    }

    public static void writeIntBe(byte[] data, int offset, int value) {
        Preconditions.checkPositionIndexes(offset, offset + 4, data.length);
        data[offset] = (byte) (value >>> 24);
        data[offset + 1] = (byte) (value >>> 16);
        data[offset + 2] = (byte) (value >>> 8);
        data[offset + 3] = (byte) value;
    }

    public static void writeIntLe(byte[] data, int offset, int value) {
        Preconditions.checkPositionIndexes(offset, offset + 4, data.length);
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
    }

    public static long readLongBe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 8, data.length);
        return (((long) data[offset] & 0xFF) << 56)
             | (((long) data[offset + 1] & 0xFF) << 48)
             | (((long) data[offset + 2] & 0xFF) << 40)
             | (((long) data[offset + 3] & 0xFF) << 32)
             | (((long) data[offset + 4] & 0xFF) << 24)
             | (((long) data[offset + 5] & 0xFF) << 16)
             | (((long) data[offset + 6] & 0xFF) << 8)
             | ((long) data[offset + 7] & 0xFF);
    }

    public static long readLongLe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 8, data.length);
        return ((long) data[offset] & 0xFF)
             | (((long) data[offset + 1] & 0xFF) << 8)
             | (((long) data[offset + 2] & 0xFF) << 16)
             | (((long) data[offset + 3] & 0xFF) << 24)
             | (((long) data[offset + 4] & 0xFF) << 32)
             | (((long) data[offset + 5] & 0xFF) << 40)
             | (((long) data[offset + 6] & 0xFF) << 48)
             | (((long) data[offset + 7] & 0xFF) << 56);
    }

    public static void writeLongBe(byte[] data, int offset, long value) {
        Preconditions.checkPositionIndexes(offset, offset + 8, data.length);
        data[offset] = (byte) (value >>> 56);
        data[offset + 1] = (byte) (value >>> 48);
        data[offset + 2] = (byte) (value >>> 40);
        data[offset + 3] = (byte) (value >>> 32);
        data[offset + 4] = (byte) (value >>> 24);
        data[offset + 5] = (byte) (value >>> 16);
        data[offset + 6] = (byte) (value >>> 8);
        data[offset + 7] = (byte) value;
    }

    public static void writeLongLe(byte[] data, int offset, long value) {
        Preconditions.checkPositionIndexes(offset, offset + 8, data.length);
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
        data[offset + 4] = (byte) (value >>> 32);
        data[offset + 5] = (byte) (value >>> 40);
        data[offset + 6] = (byte) (value >>> 48);
        data[offset + 7] = (byte) (value >>> 56);
    }

    public static short readShortBe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 2, data.length);
        return (short) (((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF));
    }

    public static short readShortLe(byte[] data, int offset) {
        Preconditions.checkPositionIndexes(offset, offset + 2, data.length);
        return (short) ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8));
    }

    public static void writeShortBe(byte[] data, int offset, short value) {
        Preconditions.checkPositionIndexes(offset, offset + 2, data.length);
        data[offset] = (byte) (value >>> 8);
        data[offset + 1] = (byte) value;
    }

    public static void writeShortLe(byte[] data, int offset, short value) {
        Preconditions.checkPositionIndexes(offset, offset + 2, data.length);
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] fromHexString(String s) {
        int len = s.length();
        Preconditions.checkArgument(len % 2 == 0, "Hex string must have an even length");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}

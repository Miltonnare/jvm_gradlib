package com.aura.internal;

/**
 * Custom hexadecimal encoder and decoder for byte streams.
 */
public final class HexUtils {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private HexUtils() {
        // Prevent instantiation
    }

    /**
     * Encodes a byte array to a hexadecimal string.
     */
    public static String encode(byte[] data) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        char[] out = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xFF;
            out[i * 2] = HEX_CHARS[v >>> 4];
            out[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * Decodes a hexadecimal string back to a byte array.
     */
    public static byte[] decode(String hex) {
        Preconditions.checkNotNull(hex, "Hex string cannot be null");
        String clean = hex.trim();
        if (clean.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }

        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int high = parseDigit(clean.charAt(i * 2), i * 2);
            int low = parseDigit(clean.charAt(i * 2 + 1), i * 2 + 1);
            out[i] = (byte) ((high << 4) | low);
        }
        return out;
    }

    private static int parseDigit(char c, int pos) {
        int v = Character.digit(c, 16);
        if (v == -1) {
            throw new IllegalArgumentException("Invalid hexadecimal character '" + c + "' at position " + pos);
        }
        return v;
    }
}

package com.aura.codec.compression;

import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;

/**
 * Implementation of Run-Length Encoding (RLE) using a PackBits-like scheme.
 */
public final class RleCompression {

    private RleCompression() {
        // Prevent instantiation
    }

    /**
     * Encodes byte array using RLE.
     */
    public static byte[] compress(byte[] data) {
        Preconditions.checkNotNull(data, "Input data cannot be null");
        if (data.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < data.length) {
            // Check for runs of repeating bytes
            int runLen = 1;
            while (i + runLen < data.length && data[i] == data[i + runLen] && runLen < 130) {
                runLen++;
            }

            if (runLen >= 3) {
                // Write run control byte (MSB is 1)
                // runLen 3 -> control byte 0x80 (128)
                // runLen 130 -> control byte 0xFF (255)
                int control = 0x80 | (runLen - 3);
                out.write(control);
                out.write(data[i]);
                i += runLen;
            } else {
                // Write literal sequence (MSB is 0)
                int literalLen = 0;
                // Look ahead for runs to stop compiling literals
                while (i + literalLen < data.length && literalLen < 128) {
                    if (i + literalLen + 2 < data.length 
                        && data[i + literalLen] == data[i + literalLen + 1] 
                        && data[i + literalLen] == data[i + literalLen + 2]) {
                        // Found a repeating run of at least 3, stop literal compilation
                        break;
                    }
                    literalLen++;
                }

                // Write literal control byte (literalLen - 1)
                int control = literalLen - 1;
                out.write(control);
                out.write(data, i, literalLen);
                i += literalLen;
            }
        }
        return out.toByteArray();
    }

    /**
     * Decodes RLE encoded byte array.
     */
    public static byte[] decompress(byte[] compressed, int maxUncompressedSize) {
        Preconditions.checkNotNull(compressed, "Compressed data cannot be null");
        Preconditions.checkArgument(maxUncompressedSize >= 0, "Max uncompressed size must be non-negative");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < compressed.length) {
            int control = compressed[i] & 0xFF;
            i++;

            if ((control & 0x80) != 0) {
                // Run of repeating bytes
                int runLen = (control & 0x7F) + 3;
                if (i >= compressed.length) {
                    throw new IllegalArgumentException("Truncated RLE run data");
                }
                int value = compressed[i] & 0xFF;
                i++;

                if (out.size() + runLen > maxUncompressedSize) {
                    throw new IllegalStateException("Decompressed size exceeds allowed limit: " + maxUncompressedSize);
                }

                for (int j = 0; j < runLen; j++) {
                    out.write(value);
                }
            } else {
                // Literal bytes
                int literalLen = control + 1;
                if (i + literalLen > compressed.length) {
                    throw new IllegalArgumentException("Truncated RLE literal data");
                }

                if (out.size() + literalLen > maxUncompressedSize) {
                    throw new IllegalStateException("Decompressed size exceeds allowed limit: " + maxUncompressedSize);
                }

                out.write(compressed, i, literalLen);
                i += literalLen;
            }
        }
        return out.toByteArray();
    }
}

package com.aura.stream;

import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream wrapper that allows writing individual bits.
 */
public class BitOutputStream implements AutoCloseable {
    private final OutputStream out;
    private long bitBuffer;
    private int bitsInBuffer;

    public BitOutputStream(OutputStream out) {
        this.out = Preconditions.checkNotNull(out, "Output stream cannot be null");
    }

    /**
     * Writes the lowest numBits of value to the output stream.
     */
    public void writeBits(int value, int numBits) throws IOException {
        Preconditions.checkArgument(numBits >= 0 && numBits <= 32, "Must write between 0 and 32 bits");
        if (numBits == 0) {
            return;
        }

        // Mask value to avoid overflow bits pollution
        long maskedValue = value & ((1L << numBits) - 1);
        bitBuffer |= maskedValue << bitsInBuffer;
        bitsInBuffer += numBits;

        while (bitsInBuffer >= 8) {
            out.write((int) (bitBuffer & 0xFF));
            bitBuffer >>>= 8;
            bitsInBuffer -= 8;
        }
    }

    /**
     * Writes a single bit (0 or 1).
     */
    public void writeBit(int bit) throws IOException {
        writeBits(bit, 1);
    }

    /**
     * Flushes any buffered bits to the stream, padding with zeros if necessary.
     */
    public void flushBits() throws IOException {
        if (bitsInBuffer > 0) {
            out.write((int) (bitBuffer & 0xFF));
            bitBuffer = 0;
            bitsInBuffer = 0;
        }
    }

    @Override
    public void close() throws IOException {
        flushBits();
        out.close();
    }
}

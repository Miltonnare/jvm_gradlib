package com.aura.stream;

import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream wrapper that allows reading individual bits.
 */
public class BitInputStream implements AutoCloseable {
    private final InputStream in;
    private long bitBuffer;
    private int bitsInBuffer;

    public BitInputStream(InputStream in) {
        this.in = Preconditions.checkNotNull(in, "Input stream cannot be null");
    }

    /**
     * Reads the requested number of bits (up to 32) and returns them as an integer.
     */
    public int readBits(int numBits) throws IOException {
        Preconditions.checkArgument(numBits >= 0 && numBits <= 32, "Must read between 0 and 32 bits");
        if (numBits == 0) {
            return 0;
        }

        while (bitsInBuffer < numBits) {
            int nextByte = in.read();
            if (nextByte == -1) {
                if (bitsInBuffer == 0) {
                    return -1; // EOF
                }
                // Yield remaining bits
                int result = (int) (bitBuffer & ((1L << bitsInBuffer) - 1));
                bitsInBuffer = 0;
                return result;
            }
            bitBuffer |= ((long) nextByte) << bitsInBuffer;
            bitsInBuffer += 8;
        }

        int result = (int) (bitBuffer & ((1L << numBits) - 1));
        bitBuffer >>>= numBits;
        bitsInBuffer -= numBits;
        return result;
    }

    /**
     * Reads a single bit. Returns 0, 1, or -1 on EOF.
     */
    public int readBit() throws IOException {
        return readBits(1);
    }

    /**
     * Aligns the read pointer to the next byte boundary, discarding any buffered bits.
     */
    public void align() {
        bitBuffer = 0;
        bitsInBuffer = 0;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}

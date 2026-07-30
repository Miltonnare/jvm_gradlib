package com.aura.stream;

import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream wrapper that limits the number of bytes that can be read.
 */
public class BoundedInputStream extends InputStream {
    private final InputStream in;
    private final long maxBytes;
    private long bytesRead;

    public BoundedInputStream(InputStream in, long maxBytes) {
        this.in = Preconditions.checkNotNull(in, "Input stream cannot be null");
        Preconditions.checkArgument(maxBytes >= 0, "Max bytes cannot be negative");
        this.maxBytes = maxBytes;
        this.bytesRead = 0;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= maxBytes) {
            return -1; // End of boundary reached
        }
        int result = in.read();
        if (result != -1) {
            bytesRead++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        if (bytesRead >= maxBytes) {
            return -1;
        }
        long remaining = maxBytes - bytesRead;
        int toRead = (int) Math.min(len, remaining);
        int result = in.read(b, off, toRead);
        if (result != -1) {
            bytesRead += result;
        }
        return result;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), maxBytes - bytesRead);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}

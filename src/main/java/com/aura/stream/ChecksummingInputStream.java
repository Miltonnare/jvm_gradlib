package com.aura.stream;

import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream wrapper that computes a CRC32 checksum as bytes are read.
 */
public class ChecksummingInputStream extends InputStream {
    private final InputStream in;
    private final Crc32 crc = new Crc32();

    public ChecksummingInputStream(InputStream in) {
        this.in = Preconditions.checkNotNull(in, "Input stream cannot be null");
    }

    @Override
    public int read() throws IOException {
        int val = in.read();
        if (val != -1) {
            crc.update(val);
        }
        return val;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        int result = in.read(b, off, len);
        if (result != -1) {
            crc.update(b, off, result);
        }
        return result;
    }

    public long getChecksum() {
        return crc.getValue();
    }

    public void resetChecksum() {
        crc.reset();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}

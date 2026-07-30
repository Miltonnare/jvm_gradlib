package com.aura.stream;

import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream wrapper that computes a CRC32 checksum as bytes are written.
 */
public class ChecksummingOutputStream extends OutputStream {
    private final OutputStream out;
    private final Crc32 crc = new Crc32();

    public ChecksummingOutputStream(OutputStream out) {
        this.out = Preconditions.checkNotNull(out, "Output stream cannot be null");
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        crc.update(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        out.write(b, off, len);
        crc.update(b, off, len);
    }

    public long getChecksum() {
        return crc.getValue();
    }

    public void resetChecksum() {
        crc.reset();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}

package com.aura.stream;

import com.aura.internal.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An in-memory seekable byte buffer that can be read from and written to.
 */
public class SeekingBuffer {
    private byte[] buf;
    private int size;
    private int position;

    public SeekingBuffer() {
        this(128);
    }

    public SeekingBuffer(int initialCapacity) {
        Preconditions.checkArgument(initialCapacity > 0, "Capacity must be positive");
        this.buf = new byte[initialCapacity];
        this.size = 0;
        this.position = 0;
    }

    public SeekingBuffer(byte[] initialData) {
        Preconditions.checkNotNull(initialData, "Data cannot be null");
        this.buf = Arrays.copyOf(initialData, initialData.length);
        this.size = initialData.length;
        this.position = 0;
    }

    public int position() {
        return position;
    }

    public int size() {
        return size;
    }

    public void seek(int pos) {
        Preconditions.checkArgument(pos >= 0, "Position cannot be negative");
        this.position = pos;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(buf, size);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0) {
            int newCapacity = buf.length * 2;
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            buf = Arrays.copyOf(buf, newCapacity);
        }
    }

    public void write(int b) {
        ensureCapacity(position + 1);
        buf[position] = (byte) b;
        position++;
        if (position > size) {
            size = position;
        }
    }

    public void write(byte[] b, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        ensureCapacity(position + len);
        System.arraycopy(b, off, buf, position, len);
        position += len;
        if (position > size) {
            size = position;
        }
    }

    public int read() {
        if (position >= size) {
            return -1;
        }
        int val = buf[position] & 0xFF;
        position++;
        return val;
    }

    public int read(byte[] b, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        if (position >= size) {
            return -1;
        }
        int available = size - position;
        int toRead = Math.min(len, available);
        System.arraycopy(buf, position, b, off, toRead);
        position += toRead;
        return toRead;
    }

    public InputStream asInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return SeekingBuffer.this.read();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return SeekingBuffer.this.read(b, off, len);
            }

            @Override
            public int available() {
                return size - position;
            }
        };
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                SeekingBuffer.this.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                SeekingBuffer.this.write(b, off, len);
            }
        };
    }
}

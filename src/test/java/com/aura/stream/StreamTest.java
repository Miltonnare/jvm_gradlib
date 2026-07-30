package com.aura.stream;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class StreamTest {

    @Test
    public void testBitStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBit(1);
            bos.writeBit(0);
            bos.writeBits(0x0F, 4); // writes 111100 to buffer: 1, 0, 1, 1, 1, 1 (reverse bit layout)
            bos.writeBits(17, 8);  // writes 17 (00010001)
        }

        byte[] output = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(output);
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(1, bis.readBit());
            assertEquals(0, bis.readBit());
            assertEquals(0x0F, bis.readBits(4));
            assertEquals(17, bis.readBits(8));
            assertEquals(0, bis.readBit()); // padding bit
            assertEquals(0, bis.readBit()); // padding bit
            assertEquals(-1, bis.readBit()); // EOF
        }
    }

    @Test
    public void testSeekingBuffer() {
        SeekingBuffer sb = new SeekingBuffer(10);
        sb.write(1);
        sb.write(2);
        sb.write(new byte[]{3, 4, 5}, 0, 3);
        assertEquals(5, sb.size());
        assertEquals(5, sb.position());

        sb.seek(1);
        assertEquals(2, sb.read());
        assertEquals(3, sb.read());

        sb.seek(0);
        byte[] copy = sb.getBytes();
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, copy);
    }

    @Test
    public void testBoundedInputStream() throws IOException {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BoundedInputStream bis = new BoundedInputStream(bais, 5);

        assertEquals(5, bis.available());
        assertEquals(1, bis.read());
        byte[] buffer = new byte[10];
        int read = bis.read(buffer, 0, 10);
        assertEquals(4, read);
        assertArrayEquals(new byte[]{2, 3, 4, 5, 0, 0, 0, 0, 0, 0}, buffer);
        assertEquals(-1, bis.read());
    }

    @Test
    public void testChecksummingStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChecksummingOutputStream cos = new ChecksummingOutputStream(baos);
        cos.write("Hello".getBytes());
        long writeChecksum = cos.getChecksum();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ChecksummingInputStream cis = new ChecksummingInputStream(bais);
        byte[] buf = new byte[10];
        int read = cis.read(buf);
        assertEquals(5, read);
        assertEquals(writeChecksum, cis.getChecksum());
    }
}

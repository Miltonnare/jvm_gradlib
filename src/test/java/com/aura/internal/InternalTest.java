package com.aura.internal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InternalTest {

    @Test
    public void testPreconditions() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkArgument(false, "err"));
        assertThrows(IllegalStateException.class, () -> Preconditions.checkState(false, "err"));
        assertThrows(NullPointerException.class, () -> Preconditions.checkNotNull(null, "err"));
        assertEquals("test", Preconditions.checkNotNull("test", "err"));
        assertThrows(IndexOutOfBoundsException.class, () -> Preconditions.checkPositionIndexes(-1, 5, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> Preconditions.checkPositionIndexes(5, 4, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> Preconditions.checkPositionIndexes(0, 11, 10));
        assertDoesNotThrow(() -> Preconditions.checkPositionIndexes(0, 10, 10));
    }

    @Test
    public void testByteUtils() {
        byte[] buf = new byte[16];
        ByteUtils.writeIntBe(buf, 0, 0x12345678);
        assertEquals(0x12345678, ByteUtils.readIntBe(buf, 0));
        assertEquals(0x78563412, ByteUtils.readIntLe(buf, 0));

        ByteUtils.writeIntLe(buf, 4, 0x0ABCDEF0);
        assertEquals(0x0ABCDEF0, ByteUtils.readIntLe(buf, 4));

        ByteUtils.writeLongBe(buf, 8, 0x1122334455667788L);
        assertEquals(0x1122334455667788L, ByteUtils.readLongBe(buf, 8));
        assertEquals(0x8877665544332211L, ByteUtils.readLongLe(buf, 8));

        byte[] hexBuf = ByteUtils.fromHexString("aabbcc0011");
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, 0x00, 0x11}, hexBuf);
        assertEquals("aabbcc0011", ByteUtils.toHexString(hexBuf));

        assertThrows(IllegalArgumentException.class, () -> ByteUtils.fromHexString("abc"));
    }

    @Test
    public void testCrc32() {
        byte[] data = "Hello World!".getBytes();
        long crc = Crc32.compute(data);
        assertEquals(0x1C291CA3L, crc);

        Crc32 c = new Crc32();
        c.update(data, 0, 5); // "Hello"
        c.update(' ');
        c.update("World!".getBytes(), 0, 6);
        assertEquals(0x1C291CA3L, c.getValue());

        c.reset();
        c.update(data, 0, data.length);
        assertEquals(0x1C291CA3L, c.getValue());
    }
}

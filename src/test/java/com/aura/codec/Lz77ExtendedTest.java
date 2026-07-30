package com.aura.codec;

import com.aura.codec.compression.Lz77Compression;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Lz77ExtendedTest {

    @Test
    public void testLz77Empty() throws java.io.IOException {
        byte[] empty = new byte[0];
        assertArrayEquals(empty, Lz77Compression.compress(empty));
        assertArrayEquals(empty, Lz77Compression.decompress(empty, 100));
    }
}

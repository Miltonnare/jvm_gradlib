package com.aura.codec;

import com.aura.codec.compression.WordRleCompression;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class WordRleTest {

    @Test
    public void testWordCompression() throws IOException {
        String single = "the quick brown fox jumps over the lazy dog. ";
        String text = single.repeat(10);
        byte[] compressed = WordRleCompression.compress(text);

        assertNotNull(compressed);
        assertTrue(compressed.length < text.getBytes().length, "Word compression should reduce text size");

        String decompressed = WordRleCompression.decompress(compressed);
        assertEquals(text, decompressed);
    }
}

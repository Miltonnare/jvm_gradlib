package com.aura.codec;

import com.aura.codec.encryption.AuraKeyRing;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class KeyRingTest {

    @Test
    public void testKeyRingSerializeAndParse() throws IOException {
        AuraKeyRing keyring = new AuraKeyRing();
        byte[] key1 = {1, 2, 3, 4};
        byte[] iv1 = {5, 6, 7, 8};
        keyring.addKey("default-xor", (byte) 1, key1, null);
        keyring.addKey("test-aes", (byte) 2, key1, iv1);

        byte[] serialized = keyring.serialize();
        assertNotNull(serialized);
        assertTrue(serialized.length > 20);

        AuraKeyRing parsed = AuraKeyRing.parse(serialized);
        assertEquals(2, parsed.getKeys().size());
        
        var entry1 = parsed.getKey("default-xor");
        assertEquals(1, entry1.getAlgorithm());
        assertArrayEquals(key1, entry1.getKey());
        assertNull(entry1.getIv());

        var entry2 = parsed.getKey("test-aes");
        assertEquals(2, entry2.getAlgorithm());
        assertArrayEquals(key1, entry2.getKey());
        assertArrayEquals(iv1, entry2.getIv());
    }

    @Test
    public void testKeyRingValidationErrors() {
        byte[] invalidData = new byte[10]; // too short
        assertThrows(IllegalArgumentException.class, () -> AuraKeyRing.parse(invalidData));

        byte[] badMagic = {(byte) 'B', (byte) 'A', (byte) 'D', (byte) 'S', 1, 0, 0, 0, 0, 0, 0, 0};
        assertThrows(IllegalArgumentException.class, () -> AuraKeyRing.parse(badMagic));
    }
}

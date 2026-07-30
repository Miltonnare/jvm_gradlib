package com.aura.internal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HexUtilsTest {

    @Test
    public void testHexEncodingAndDecoding() {
        byte[] data = {0, 1, 10, 15, 16, 127, -1, -128};
        String hex = HexUtils.encode(data);
        assertEquals("00010a0f107fff80", hex);

        byte[] decoded = HexUtils.decode(hex);
        assertArrayEquals(data, decoded);
    }

    @Test
    public void testHexErrors() {
        assertThrows(IllegalArgumentException.class, () -> HexUtils.decode("0a1")); // odd length
        assertThrows(IllegalArgumentException.class, () -> HexUtils.decode("0g"));  // invalid character
    }
}

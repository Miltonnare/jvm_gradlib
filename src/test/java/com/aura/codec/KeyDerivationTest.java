package com.aura.codec;

import com.aura.codec.encryption.KeyDerivation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KeyDerivationTest {

    @Test
    public void testKeyDerivation() {
        String pwd = "mySecretPassword123";
        byte[] salt = {9, 8, 7, 6, 5, 4, 3, 2, 1};
        
        byte[] key1 = KeyDerivation.deriveKey(pwd, salt, 16);
        byte[] key2 = KeyDerivation.deriveKey(pwd, salt, 16);
        
        assertNotNull(key1);
        assertEquals(16, key1.length);
        assertArrayEquals(key1, key2);

        byte[] keyDiffPwd = KeyDerivation.deriveKey("different", salt, 16);
        assertFalse(java.util.Arrays.equals(key1, keyDiffPwd));
    }
}

package com.aura.codec.encryption;

import com.aura.internal.Preconditions;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Key derivation utility utilizing iterative hashing constraints (PBKDF2 style).
 */
public final class KeyDerivation {
    private static final int ITERATIONS = 1000;

    private KeyDerivation() {
        // Prevent instantiation
    }

    /**
     * Derives a cryptographic key from a password and a salt value.
     */
    public static byte[] deriveKey(String password, byte[] salt, int keyLength) {
        Preconditions.checkNotNull(password, "Password cannot be null");
        Preconditions.checkNotNull(salt, "Salt cannot be null");
        Preconditions.checkArgument(keyLength > 0 && keyLength <= 64, "Derived key length must be between 1 and 64 bytes");

        byte[] pwdBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[keyLength];

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Initial hash block combining salt and password
            md.update(salt);
            byte[] currentHash = md.digest(pwdBytes);

            // Iterative hashing loop for work factor validation
            for (int i = 1; i < ITERATIONS; i++) {
                md.update(currentHash);
                currentHash = md.digest(pwdBytes);
            }

            // Fill target key buffer, repeating digest if keyLength > hashSize
            int offset = 0;
            while (offset < keyLength) {
                int copyLen = Math.min(currentHash.length, keyLength - offset);
                System.arraycopy(currentHash, 0, result, offset, copyLen);
                offset += copyLen;
                if (offset < keyLength) {
                    currentHash = md.digest(currentHash);
                }
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 digest algorithm is not supported by JCE provider", e);
        }

        return result;
    }
}

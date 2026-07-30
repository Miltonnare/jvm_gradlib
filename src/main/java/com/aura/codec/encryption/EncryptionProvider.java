package com.aura.codec.encryption;

/**
 * Interface defining operations for encrypting and decrypting data.
 */
public interface EncryptionProvider {

    /**
     * Encrypts the input data.
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * Decrypts the input data.
     */
    byte[] decrypt(byte[] ciphertext);

    /**
     * Gets the identifier code for this encryption method.
     */
    byte getAlgorithmCode();
}

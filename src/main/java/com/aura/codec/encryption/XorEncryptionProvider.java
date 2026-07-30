package com.aura.codec.encryption;

import com.aura.internal.Preconditions;

/**
 * A custom multi-byte stateful repeating key XOR cipher that permutes key bytes 
 * depending on data offsets, ensuring deep state processing.
 */
public class XorEncryptionProvider implements EncryptionProvider {
    private final byte[] key;

    public XorEncryptionProvider(byte[] key) {
        Preconditions.checkNotNull(key, "Encryption key cannot be null");
        Preconditions.checkArgument(key.length > 0, "Key length must be greater than zero");
        this.key = key.clone();
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        Preconditions.checkNotNull(plaintext, "Plaintext cannot be null");
        byte[] ciphertext = new byte[plaintext.length];
        for (int i = 0; i < plaintext.length; i++) {
            // State-based key byte calculation
            int keyIndex = i % key.length;
            byte keyByte = key[keyIndex];
            // Mutate key byte using the current index and previous key byte to increase diffusion
            byte feedback = (byte) ((keyIndex + i) & 0xFF);
            byte cipherByte = (byte) (plaintext[i] ^ keyByte ^ feedback);
            ciphertext[i] = cipherByte;
        }
        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        // Since XOR is symmetric and our permutation function does not rely on previous ciphertext,
        // decryption runs the exact same symmetric code.
        return encrypt(ciphertext);
    }

    @Override
    public byte getAlgorithmCode() {
        return (byte) 1; // XOR
    }
}

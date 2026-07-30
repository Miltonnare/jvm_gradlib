package com.aura.codec.encryption;

import com.aura.internal.Preconditions;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * Standard-compliant block cipher provider wrapping JCE's AES/CBC/PKCS5Padding.
 */
public class CipherEncryptionProvider implements EncryptionProvider {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final byte[] keyBytes;
    private final byte[] ivBytes;

    public CipherEncryptionProvider(byte[] keyBytes, byte[] ivBytes) {
        Preconditions.checkNotNull(keyBytes, "Key bytes cannot be null");
        Preconditions.checkNotNull(ivBytes, "IV bytes cannot be null");
        // AES key size check (16, 24, 32 bytes)
        Preconditions.checkArgument(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32,
                "AES key must be 16, 24, or 32 bytes long");
        Preconditions.checkArgument(ivBytes.length == 16, "AES IV must be 16 bytes long");
        
        this.keyBytes = keyBytes.clone();
        this.ivBytes = ivBytes.clone();
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        Preconditions.checkNotNull(plaintext, "Plaintext cannot be null");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            return cipher.doFinal(plaintext);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        Preconditions.checkNotNull(ciphertext, "Ciphertext cannot be null");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    @Override
    public byte getAlgorithmCode() {
        return (byte) 2; // AES-CBC
    }
}

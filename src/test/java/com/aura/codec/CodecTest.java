package com.aura.codec;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.compression.CompressionProvider;
import com.aura.codec.compression.Lz77Compression;
import com.aura.codec.compression.RleCompression;
import com.aura.codec.encryption.CipherEncryptionProvider;
import com.aura.codec.encryption.XorEncryptionProvider;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class CodecTest {

    @Test
    public void testRleCompression() {
        byte[] input = "AAAAABBBCCCDDDDDDEEEEEEE".getBytes();
        byte[] compressed = RleCompression.compress(input);
        byte[] decompressed = RleCompression.decompress(compressed, 100);
        assertArrayEquals(input, decompressed);

        // Test literals
        byte[] literalInput = "abcdefg".getBytes();
        byte[] literalComp = RleCompression.compress(literalInput);
        assertArrayEquals(literalInput, RleCompression.decompress(literalComp, 100));

        // Test bound limits
        assertThrows(IllegalStateException.class, () -> RleCompression.decompress(compressed, 5));
    }

    @Test
    public void testLz77Compression() throws IOException {
        byte[] input = "the quick brown fox jumps over the lazy dog. the quick brown fox jumps over the lazy dog.".getBytes();
        byte[] compressed = Lz77Compression.compress(input);
        byte[] decompressed = Lz77Compression.decompress(compressed, 500);
        assertArrayEquals(input, decompressed);

        // Test compression provider wrapper
        byte[] provComp = CompressionProvider.compress(input, CompressionAlgorithm.LZ77);
        byte[] provDecomp = CompressionProvider.decompress(provComp, CompressionAlgorithm.LZ77, 500);
        assertArrayEquals(input, provDecomp);

        // Test algorithms NONE and RLE integration in provider
        byte[] noneComp = CompressionProvider.compress(input, CompressionAlgorithm.NONE);
        assertArrayEquals(input, noneComp);
        assertArrayEquals(input, CompressionProvider.decompress(noneComp, CompressionAlgorithm.NONE, 500));
    }

    @Test
    public void testXorEncryption() {
        byte[] key = {0x12, 0x34, 0x56, 0x78};
        XorEncryptionProvider provider = new XorEncryptionProvider(key);
        byte[] plain = "TestDataPayload".getBytes();
        byte[] cipher = provider.encrypt(plain);
        assertFalse(Arrays.equals(plain, cipher));
        byte[] dec = provider.decrypt(cipher);
        assertArrayEquals(plain, dec);
        assertEquals(1, provider.getAlgorithmCode());
    }

    @Test
    public void testCipherEncryption() {
        byte[] key = new byte[32]; // AES 256
        byte[] iv = new byte[16];
        Arrays.fill(key, (byte) 1);
        Arrays.fill(iv, (byte) 2);

        CipherEncryptionProvider provider = new CipherEncryptionProvider(key, iv);
        byte[] plain = "SecretMessageToBeEncrypted123456".getBytes();
        byte[] cipher = provider.encrypt(plain);
        byte[] dec = provider.decrypt(cipher);
        assertArrayEquals(plain, dec);
        assertEquals(2, provider.getAlgorithmCode());
    }
}

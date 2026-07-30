package com.aura.codec.encryption;

import com.aura.internal.ByteUtils;
import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages serialization and parsing of cryptographic keyrings in a secure binary format.
 */
public class AuraKeyRing {
    private static final byte[] MAGIC = {(byte) 'K', (byte) 'Y', (byte) 'R', (byte) 'G'};
    private static final int MAX_KEYS = 256;
    private static final int MAX_KEY_SIZE = 128;
    private static final int MAX_NAME_SIZE = 256;

    private final Map<String, KeyEntry> keys = new LinkedHashMap<>();

    public static final class KeyEntry {
        private final byte algorithm;
        private final byte[] key;
        private final byte[] iv;

        public KeyEntry(byte algorithm, byte[] key, byte[] iv) {
            this.algorithm = algorithm;
            this.key = Preconditions.checkNotNull(key, "Key bytes cannot be null").clone();
            this.iv = iv != null ? iv.clone() : null;
        }

        public byte getAlgorithm() { return algorithm; }
        public byte[] getKey() { return key.clone(); }
        public byte[] getIv() { return iv != null ? iv.clone() : null; }
    }

    public void addKey(String name, byte algorithm, byte[] key, byte[] iv) {
        Preconditions.checkNotNull(name, "Key name cannot be null");
        Preconditions.checkArgument(!name.isEmpty(), "Key name cannot be empty");
        keys.put(name, new KeyEntry(algorithm, key, iv));
    }

    public KeyEntry getKey(String name) {
        return keys.get(name);
    }

    public Map<String, KeyEntry> getKeys() {
        return Collections.unmodifiableMap(keys);
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 1. Write Header
        baos.write(MAGIC);
        baos.write(1); // Version 1
        
        byte[] countBytes = new byte[2];
        ByteUtils.writeShortBe(countBytes, 0, (short) keys.size());
        baos.write(countBytes);

        // Dummy checksum slot
        byte[] crcBuf = new byte[4];
        baos.write(crcBuf);

        // 2. Write Entries
        ByteArrayOutputStream entriesOs = new ByteArrayOutputStream();
        for (Map.Entry<String, KeyEntry> entry : keys.entrySet()) {
            byte[] nameBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            Preconditions.checkArgument(nameBytes.length <= MAX_NAME_SIZE, "Key name too long");
            
            byte[] nameLenBuf = new byte[2];
            ByteUtils.writeShortBe(nameLenBuf, 0, (short) nameBytes.length);
            entriesOs.write(nameLenBuf);
            entriesOs.write(nameBytes);

            KeyEntry keyEntry = entry.getValue();
            entriesOs.write(keyEntry.algorithm);

            byte[] keyLenBuf = new byte[2];
            ByteUtils.writeShortBe(keyLenBuf, 0, (short) keyEntry.key.length);
            entriesOs.write(keyLenBuf);
            entriesOs.write(keyEntry.key);

            byte[] ivLenBuf = new byte[2];
            if (keyEntry.iv != null) {
                ByteUtils.writeShortBe(ivLenBuf, 0, (short) keyEntry.iv.length);
                entriesOs.write(ivLenBuf);
                entriesOs.write(keyEntry.iv);
            } else {
                ByteUtils.writeShortBe(ivLenBuf, 0, (short) 0);
                entriesOs.write(ivLenBuf);
            }
        }

        byte[] serializedEntries = entriesOs.toByteArray();
        long crc = Crc32.compute(serializedEntries);

        byte[] finalBytes = baos.toByteArray();
        // Write actual checksum
        ByteUtils.writeIntBe(finalBytes, 7, (int) crc);

        // Write entries
        ByteArrayOutputStream finalOs = new ByteArrayOutputStream();
        finalOs.write(finalBytes);
        finalOs.write(serializedEntries);

        return finalOs.toByteArray();
    }

    public static AuraKeyRing parse(byte[] data) throws IOException {
        Preconditions.checkNotNull(data, "Data cannot be null");
        if (data.length < 11) {
            throw new IllegalArgumentException("KeyRing data truncated");
        }

        // 1. Verify Magic
        for (int i = 0; i < 4; i++) {
            if (data[i] != MAGIC[i]) {
                throw new IllegalArgumentException("Invalid KeyRing magic signature");
            }
        }

        byte version = data[4];
        if (version != 1) {
            throw new IllegalArgumentException("Unsupported KeyRing version: " + version);
        }

        int count = ByteUtils.readShortBe(data, 5) & 0xFFFF;
        if (count < 0 || count > MAX_KEYS) {
            throw new IllegalArgumentException("Invalid KeyRing key count: " + count);
        }

        long expectedCrc = ByteUtils.readIntBe(data, 7) & 0xFFFFFFFFL;
        long computedCrc = Crc32.compute(data, 11, data.length - 11);
        if (expectedCrc != computedCrc) {
            throw new IllegalArgumentException(String.format("KeyRing checksum mismatch: expected 0x%X, got 0x%X",
                expectedCrc, computedCrc));
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data, 11, data.length - 11);
        AuraKeyRing keyring = new AuraKeyRing();

        for (int i = 0; i < count; i++) {
            byte[] nameLenBuf = new byte[2];
            if (readFully(bais, nameLenBuf) < 2) throw new IllegalArgumentException("Truncated KeyRing key name size");
            int nameLen = ByteUtils.readShortBe(nameLenBuf, 0) & 0xFFFF;
            if (nameLen > MAX_NAME_SIZE) throw new IllegalArgumentException("Key name exceeds size limit");

            byte[] nameBytes = new byte[nameLen];
            if (readFully(bais, nameBytes) < nameLen) throw new IllegalArgumentException("Truncated KeyRing key name");
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            int algo = bais.read();
            if (algo == -1) throw new IllegalArgumentException("Truncated KeyRing algorithm ID");

            byte[] keyLenBuf = new byte[2];
            if (readFully(bais, keyLenBuf) < 2) throw new IllegalArgumentException("Truncated KeyRing key size");
            int keyLen = ByteUtils.readShortBe(keyLenBuf, 0) & 0xFFFF;
            if (keyLen > MAX_KEY_SIZE) throw new IllegalArgumentException("Key size exceeds safety limit");

            byte[] keyBytes = new byte[keyLen];
            if (readFully(bais, keyBytes) < keyLen) throw new IllegalArgumentException("Truncated KeyRing key bytes");

            byte[] ivLenBuf = new byte[2];
            if (readFully(bais, ivLenBuf) < 2) throw new IllegalArgumentException("Truncated KeyRing IV size");
            int ivLen = ByteUtils.readShortBe(ivLenBuf, 0) & 0xFFFF;
            if (ivLen > MAX_KEY_SIZE) throw new IllegalArgumentException("IV size exceeds safety limit");

            byte[] ivBytes = null;
            if (ivLen > 0) {
                ivBytes = new byte[ivLen];
                if (readFully(bais, ivBytes) < ivLen) throw new IllegalArgumentException("Truncated KeyRing IV bytes");
            }

            keyring.addKey(name, (byte) algo, keyBytes, ivBytes);
        }

        return keyring;
    }

    private static int readFully(InputStream in, byte[] b) throws IOException {
        int total = 0;
        while (total < b.length) {
            int result = in.read(b, total, b.length - total);
            if (result == -1) {
                return total == 0 ? -1 : total;
            }
            total += result;
        }
        return total;
    }
}

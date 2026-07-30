package com.aura.engine;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Compacts binary mutation logs by merging redundant key mutations per entity.
 * Only outputs the latest state mutation for each key.
 */
public class EventLogCompactor {
    private static final byte[] LOG_MAGIC = {(byte) 'E', (byte) 'V', (byte) 'L', (byte) 'G'};

    public byte[] compact(byte[] logData) throws IOException {
        Preconditions.checkNotNull(logData, "Log data cannot be null");
        if (logData.length < 6) {
            return new byte[0];
        }

        // 1. Verify Magic
        for (int i = 0; i < 4; i++) {
            if (logData[i] != LOG_MAGIC[i]) {
                throw new IllegalArgumentException("Invalid Event Log magic signature");
            }
        }

        int version = ByteUtils.readShortBe(logData, 4) & 0xFFFF;
        if (version != 1) {
            throw new IllegalArgumentException("Unsupported Event Log version: " + version);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(logData, 6, logData.length - 6);
        
        // Entity name -> Map of Key -> Mutation
        Map<String, Map<String, CompactedMutation>> entityMutations = new LinkedHashMap<>();

        while (bais.available() > 0) {
            byte[] lenBuf = new byte[2];
            if (readFully(bais, lenBuf) < 2) break; // End of log stream
            int nameLen = ByteUtils.readShortBe(lenBuf, 0) & 0xFFFF;

            byte[] nameBytes = new byte[nameLen];
            if (readFully(bais, nameBytes) < nameLen) {
                throw new IllegalArgumentException("Truncated entity name in log");
            }
            String entityName = new String(nameBytes, StandardCharsets.UTF_8);

            byte[] countBuf = new byte[2];
            if (readFully(bais, countBuf) < 2) {
                throw new IllegalArgumentException("Truncated mutation count in log");
            }
            int mutCount = ByteUtils.readShortBe(countBuf, 0) & 0xFFFF;

            Map<String, CompactedMutation> keyMap = entityMutations.computeIfAbsent(entityName, k -> new LinkedHashMap<>());

            for (int i = 0; i < mutCount; i++) {
                byte[] keyLenBuf = new byte[2];
                if (readFully(bais, keyLenBuf) < 2) {
                    throw new IllegalArgumentException("Truncated key length in mutation");
                }
                int keyLen = ByteUtils.readShortBe(keyLenBuf, 0) & 0xFFFF;

                byte[] keyBytes = new byte[keyLen];
                if (readFully(bais, keyBytes) < keyLen) {
                    throw new IllegalArgumentException("Truncated key string in mutation");
                }
                String key = new String(keyBytes, StandardCharsets.UTF_8);

                int tag = bais.read();
                if (tag == -1) {
                    throw new IllegalArgumentException("Truncated type tag in mutation");
                }

                byte[] valLenBuf = new byte[4];
                if (readFully(bais, valLenBuf) < 4) {
                    throw new IllegalArgumentException("Truncated value size in mutation");
                }
                int valLen = ByteUtils.readIntBe(valLenBuf, 0);
                if (valLen < 0 || valLen > 16384) {
                    throw new IllegalArgumentException("Value size exceeds limits: " + valLen);
                }

                byte[] valBytes = new byte[valLen];
                if (readFully(bais, valBytes) < valLen) {
                    throw new IllegalArgumentException("Truncated value bytes in mutation");
                }

                // Merge: overwrite existing key value with latest tag and bytes
                keyMap.put(key, new CompactedMutation(tag, valBytes));
            }
        }

        // 2. Serialize compacted state
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(LOG_MAGIC);
        
        byte[] verBuf = new byte[2];
        ByteUtils.writeShortBe(verBuf, 0, (short) 1);
        baos.write(verBuf);

        for (Map.Entry<String, Map<String, CompactedMutation>> entry : entityMutations.entrySet()) {
            Map<String, CompactedMutation> keyMap = entry.getValue();
            if (keyMap.isEmpty()) {
                continue;
            }

            byte[] nameBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] nameLenBuf = new byte[2];
            ByteUtils.writeShortBe(nameLenBuf, 0, (short) nameBytes.length);
            baos.write(nameLenBuf);
            baos.write(nameBytes);

            byte[] countBuf = new byte[2];
            ByteUtils.writeShortBe(countBuf, 0, (short) keyMap.size());
            baos.write(countBuf);

            for (Map.Entry<String, CompactedMutation> mut : keyMap.entrySet()) {
                byte[] keyBytes = mut.getKey().getBytes(StandardCharsets.UTF_8);
                byte[] keyLenBuf = new byte[2];
                ByteUtils.writeShortBe(keyLenBuf, 0, (short) keyBytes.length);
                baos.write(keyLenBuf);
                baos.write(keyBytes);

                CompactedMutation value = mut.getValue();
                baos.write(value.tag);

                byte[] valLenBuf = new byte[4];
                ByteUtils.writeIntBe(valLenBuf, 0, value.valBytes.length);
                baos.write(valLenBuf);
                baos.write(value.valBytes);
            }
        }

        return baos.toByteArray();
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

    private static class CompactedMutation {
        final int tag;
        final byte[] valBytes;

        CompactedMutation(int tag, byte[] valBytes) {
            this.tag = tag;
            this.valBytes = valBytes;
        }
    }
}

package com.aura.engine;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serializes entity context state modifications into a binary event log stream.
 */
public class EventLogWriter {
    private static final byte[] LOG_MAGIC = {(byte) 'E', (byte) 'V', (byte) 'L', (byte) 'G'};

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public EventLogWriter() throws IOException {
        // Write file header
        baos.write(LOG_MAGIC);
        byte[] ver = new byte[2];
        ByteUtils.writeShortBe(ver, 0, (short) 1);
        baos.write(ver);
    }

    /**
     * Serializes a single entity state update block with mutations.
     */
    public void writeEntityUpdate(String entityName, MapState state) throws IOException {
        Preconditions.checkNotNull(entityName, "Entity name cannot be null");
        Preconditions.checkNotNull(state, "State cannot be null");

        byte[] nameBytes = entityName.getBytes(StandardCharsets.UTF_8);
        byte[] nameLen = new byte[2];
        ByteUtils.writeShortBe(nameLen, 0, (short) nameBytes.length);
        baos.write(nameLen);
        baos.write(nameBytes);

        byte[] count = new byte[2];
        ByteUtils.writeShortBe(count, 0, (short) state.mutations.size());
        baos.write(count);

        for (var entry : state.mutations.entrySet()) {
            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] keyLen = new byte[2];
            ByteUtils.writeShortBe(keyLen, 0, (short) keyBytes.length);
            baos.write(keyLen);
            baos.write(keyBytes);

            Mutation mut = entry.getValue();
            baos.write(mut.tag);

            byte[] valLen = new byte[4];
            ByteUtils.writeIntBe(valLen, 0, mut.value.length);
            baos.write(valLen);
            baos.write(mut.value);
        }
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    public static class MapState {
        private final java.util.Map<String, Mutation> mutations = new java.util.LinkedHashMap<>();

        public void put(String key, int tag, byte[] value) {
            Preconditions.checkNotNull(key, "Key cannot be null");
            Preconditions.checkNotNull(value, "Value cannot be null");
            mutations.put(key, new Mutation(tag, value));
        }
    }

    private static class Mutation {
        final int tag;
        final byte[] value;

        Mutation(int tag, byte[] value) {
            this.tag = tag;
            this.value = value;
        }
    }
}

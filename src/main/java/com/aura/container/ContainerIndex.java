package com.aura.container;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of the complete container index block.
 */
public class ContainerIndex {
    private static final int MAX_INDEX_ENTRIES = 1024;
    private final List<ContainerIndexEntry> entries;

    public ContainerIndex(List<ContainerIndexEntry> entries) {
        this.entries = new ArrayList<>(Preconditions.checkNotNull(entries, "Entries list cannot be null"));
    }

    public static ContainerIndex parse(byte[] data) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        if (data.length < 4) {
            throw new ContainerException("Truncated index block");
        }

        int count = ByteUtils.readIntBe(data, 0);
        if (count < 0 || count > MAX_INDEX_ENTRIES) {
            throw new ContainerException("Invalid index entries count or exceeds safety limit: " + count);
        }

        List<ContainerIndexEntry> parsed = new ArrayList<>(count);
        int offset = 4;
        for (int i = 0; i < count; i++) {
            if (offset >= data.length) {
                throw new ContainerException("Premature end of index data at entry: " + i);
            }
            ContainerIndexEntry entry = ContainerIndexEntry.deserialize(data, offset);
            parsed.add(entry);
            // Entry size is 48 + name length
            int entrySize = 48 + (ByteUtils.readShortBe(data, offset + 46) & 0xFFFF);
            offset += entrySize;
        }

        return new ContainerIndex(parsed);
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] countBytes = new byte[4];
        ByteUtils.writeIntBe(countBytes, 0, entries.size());
        baos.write(countBytes);

        for (ContainerIndexEntry entry : entries) {
            baos.write(entry.serialize());
        }

        return baos.toByteArray();
    }

    public List<ContainerIndexEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public ContainerIndexEntry findEntry(String name) {
        for (ContainerIndexEntry entry : entries) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }
}

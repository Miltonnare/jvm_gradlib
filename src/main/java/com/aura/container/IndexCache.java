package com.aura.container;

import com.aura.internal.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache to store and fast-lookup container archive entry metadata.
 */
public class IndexCache {
    private final int capacity;
    private final Map<String, ContainerRecord> cacheMap;
    
    private long hitCount = 0;
    private long missCount = 0;
    private long evictionCount = 0;

    public static class ContainerRecord {
        private final long offset;
        private final int size;
        private final byte[] metadata;

        public ContainerRecord(long offset, int size, byte[] metadata) {
            this.offset = offset;
            this.size = size;
            this.metadata = metadata != null ? metadata.clone() : null;
        }

        public long getOffset() { return offset; }
        public int getSize() { return size; }
        public byte[] getMetadata() { return metadata != null ? metadata.clone() : null; }
    }

    public IndexCache(int capacity) {
        Preconditions.checkArgument(capacity > 0, "Capacity must be positive");
        this.capacity = capacity;
        this.cacheMap = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ContainerRecord> eldest) {
                if (size() > IndexCache.this.capacity) {
                    evictionCount++;
                    return true;
                }
                return false;
            }
        };
    }

    public ContainerRecord get(String entryName) {
        Preconditions.checkNotNull(entryName, "Entry name cannot be null");
        ContainerRecord record = cacheMap.get(entryName);
        if (record != null) {
            hitCount++;
        } else {
            missCount++;
        }
        return record;
    }

    public void put(String entryName, ContainerRecord record) {
        Preconditions.checkNotNull(entryName, "Entry name cannot be null");
        Preconditions.checkNotNull(record, "Record cannot be null");
        cacheMap.put(entryName, record);
    }

    public void clear() {
        cacheMap.clear();
        hitCount = 0;
        missCount = 0;
        evictionCount = 0;
    }

    public int size() {
        return cacheMap.size();
    }

    public long getHitCount() { return hitCount; }
    public long getMissCount() { return missCount; }
    public long getEvictionCount() { return evictionCount; }
}

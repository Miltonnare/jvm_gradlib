package com.aura.container;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IndexCacheTest {

    @Test
    public void testIndexCacheLru() {
        IndexCache cache = new IndexCache(2);
        IndexCache.ContainerRecord r1 = new IndexCache.ContainerRecord(100, 10, null);
        IndexCache.ContainerRecord r2 = new IndexCache.ContainerRecord(200, 20, null);
        IndexCache.ContainerRecord r3 = new IndexCache.ContainerRecord(300, 30, null);

        cache.put("entry1", r1);
        cache.put("entry2", r2);
        assertEquals(2, cache.size());

        // Get r1 to update LRU order
        assertNotNull(cache.get("entry1"));
        assertEquals(1, cache.getHitCount());

        // Add r3 which evicts entry2
        cache.put("entry3", r3);
        assertEquals(2, cache.size());
        assertNull(cache.get("entry2"));
        assertEquals(1, cache.getEvictionCount());
    }
}

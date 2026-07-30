package com.aura.metadata;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MetadataTest {

    @Test
    public void testMetadataReadWrite() throws IOException {
        Map<String, MetadataValue> map = new LinkedHashMap<>();
        map.put("version", MetadataValue.of(1));
        map.put("author", MetadataValue.of("Aura team"));
        map.put("enabled", MetadataValue.of(true));
        map.put("ratio", MetadataValue.of(2.34));
        map.put("hash", MetadataValue.of(new byte[]{1, 2, 3}));

        // Nested map
        Map<String, MetadataValue> subMap = new LinkedHashMap<>();
        subMap.put("nestedKey", MetadataValue.of("nestedVal"));
        map.put("nestedMap", MetadataValue.of(subMap));

        // Array
        List<MetadataValue> list = new ArrayList<>();
        list.add(MetadataValue.of(10));
        list.add(MetadataValue.of("item"));
        map.put("items", MetadataValue.of(list));

        byte[] serialized = BinaryMetadataWriter.write(map);
        Map<String, MetadataValue> decoded = BinaryMetadataReader.read(serialized);

        assertEquals(1, decoded.get("version").asInt());
        assertEquals("Aura team", decoded.get("author").asString());
        assertTrue(decoded.get("enabled").asBoolean());
        assertEquals(2.34, decoded.get("ratio").asDouble());
        assertArrayEquals(new byte[]{1, 2, 3}, decoded.get("hash").asBinary());
        assertEquals("nestedVal", decoded.get("nestedMap").asMap().get("nestedKey").asString());
        assertEquals(10, decoded.get("items").asArray().get(0).asInt());
        assertEquals("item", decoded.get("items").asArray().get(1).asString());
    }

    @Test
    public void testSecurityLimits() {
        // Test nesting recursion depth limit
        Map<String, MetadataValue> root = new LinkedHashMap<>();
        Map<String, MetadataValue> current = root;
        for (int i = 0; i < 20; i++) {
            Map<String, MetadataValue> next = new LinkedHashMap<>();
            current.put("child", MetadataValue.of(next));
            current = next;
        }

        assertThrows(IllegalStateException.class, () -> {
            byte[] serialized = BinaryMetadataWriter.write(root);
            BinaryMetadataReader.read(serialized);
        });
    }
}

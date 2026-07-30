package com.aura.metadata;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class BsmSchemaTest {

    @Test
    public void testSchemaValidation() {
        BsmSchema schema = new BsmSchema();
        schema.addConstraint(new BsmFieldConstraint.Builder("age", MetadataType.INTEGER)
            .required(true)
            .range(0, 100)
            .build());
        schema.addConstraint(new BsmFieldConstraint.Builder("name", MetadataType.STRING)
            .lengthBounds(2, 10)
            .build());

        Map<String, MetadataValue> valid = new HashMap<>();
        valid.put("age", MetadataValue.of(50));
        valid.put("name", MetadataValue.of("hello"));

        assertDoesNotThrow(() -> schema.validate(valid));

        // Range failure
        Map<String, MetadataValue> bad1 = new HashMap<>();
        bad1.put("age", MetadataValue.of(150));
        assertThrows(IllegalArgumentException.class, () -> schema.validate(bad1));

        // Missing required field
        Map<String, MetadataValue> bad2 = new HashMap<>();
        bad2.put("name", MetadataValue.of("hello"));
        assertThrows(IllegalArgumentException.class, () -> schema.validate(bad2));
    }
}

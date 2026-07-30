package com.aura.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XonWriterTest {

    @Test
    public void testXonSerialization() {
        XonObject root = new XonObject();
        root.put("name", new XonPrimitive("Test"));
        root.put("age", new XonPrimitive(30));
        
        XonArray array = new XonArray();
        array.add(new XonPrimitive(true));
        array.add(new XonPrimitive(false));
        root.put("flags", array);

        XonWriter writer = new XonWriter(2);
        String output = writer.write(root);

        assertNotNull(output);
        assertTrue(output.contains("name: \"Test\""));
        assertTrue(output.contains("age: 30"));
        assertTrue(output.contains("flags: [true, false]"));
    }
}

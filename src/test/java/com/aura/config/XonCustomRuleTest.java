package com.aura.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XonCustomRuleTest {

    @Test
    public void testFieldRelationRule() {
        XonCustomRule rule = new XonCustomRule("width", "height", "gt"); // width > height

        XonObject valid = new XonObject();
        valid.put("width", new XonPrimitive(200));
        valid.put("height", new XonPrimitive(100));
        assertDoesNotThrow(() -> rule.check(valid));

        XonObject invalid = new XonObject();
        invalid.put("width", new XonPrimitive(50));
        invalid.put("height", new XonPrimitive(150));
        assertThrows(IllegalArgumentException.class, () -> rule.check(invalid));
    }
}

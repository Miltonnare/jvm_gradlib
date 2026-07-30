package com.aura.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XonSchemaCompilerTest {

    @Test
    public void testSchemaCompilationAndValidation() {
        String schemaSrc = "{\n" +
                           "  type: \"object\"\n" +
                           "  requiredFields: [\"age\"]\n" +
                           "  properties: {\n" +
                           "    age: {\n" +
                           "      type: \"int\"\n" +
                           "      minimum: 0\n" +
                           "      maximum: 120\n" +
                           "    }\n" +
                           "    role: {\n" +
                           "      type: \"string\"\n" +
                           "      enum: [\"admin\", \"user\"]\n" +
                           "    }\n" +
                           "  }\n" +
                           "}";

        XonSchemaCompiler compiler = new XonSchemaCompiler();
        XonSchemaCompiler.CompiledSchema compiled = compiler.compile(schemaSrc);

        XonObject validConfig = new XonObject();
        validConfig.put("age", new XonPrimitive(35));
        validConfig.put("role", new XonPrimitive("admin"));

        assertDoesNotThrow(() -> compiled.validate(validConfig));

        // Missing required field
        XonObject invalidConfig1 = new XonObject();
        invalidConfig1.put("role", new XonPrimitive("user"));
        assertThrows(IllegalArgumentException.class, () -> compiled.validate(invalidConfig1));

        // Minimum range exceeded
        XonObject invalidConfig2 = new XonObject();
        invalidConfig2.put("age", new XonPrimitive(-5));
        assertThrows(IllegalArgumentException.class, () -> compiled.validate(invalidConfig2));
    }
}

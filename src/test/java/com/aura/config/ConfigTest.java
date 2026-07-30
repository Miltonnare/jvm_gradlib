package com.aura.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testLexer() {
        String src = """
            # This is a comment
            {
                name: "Aura Processor" // Inline comment
                version: 1.2
                debug: true
                meta: null
                tags: ["docs", "process", 100]
            }
            """;

        XonLexer lexer = new XonLexer(src);
        List<XonToken> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertTrue(tokens.size() > 10);
        assertEquals(XonToken.Type.LBRACE, tokens.get(0).getType());
        assertEquals(XonToken.Type.IDENTIFIER, tokens.get(1).getType());
        assertEquals("name", tokens.get(1).getValue());
    }

    @Test
    public void testParser() {
        String src = "{ name: 'Test', age: 30, flags: [true, false] }";
        XonLexer lexer = new XonLexer(src);
        XonParser parser = new XonParser(lexer.tokenize());
        XonValue val = parser.parse();

        assertTrue(val.isObject());
        XonObject obj = (XonObject) val;
        assertEquals("Test", obj.get("name").asString());
        assertEquals(30, obj.get("age").asInt());
        assertTrue(obj.get("flags").isArray());
        assertTrue(obj.get("flags").asArray().get(0).asBoolean());
    }

    @Test
    public void testSchemaValidator() {
        String schemaSrc = """
            {
                port: { type: "number", required: true, minimum: 1024, maximum: 65535 }
                app: { type: "string", required: true, minLength: 3, pattern: "^[a-zA-Z]+$" }
                features: { type: "array", maxItems: 3, items: { type: "string" } }
            }
            """;

        XonObject schema = (XonObject) new XonParser(new XonLexer(schemaSrc).tokenize()).parse();
        XonSchemaValidator validator = new XonSchemaValidator(schema);

        // Valid configuration
        String configOk = "{ port: 8080, app: 'Aura', features: ['a', 'b'] }";
        XonObject ok = (XonObject) new XonParser(new XonLexer(configOk).tokenize()).parse();
        assertDoesNotThrow(() -> validator.validate(ok));

        // Invalid: missing port
        String configErr1 = "{ app: 'Aura' }";
        XonObject err1 = (XonObject) new XonParser(new XonLexer(configErr1).tokenize()).parse();
        assertThrows(XonSchemaValidator.SchemaException.class, () -> validator.validate(err1));

        // Invalid: port too small
        String configErr2 = "{ port: 80, app: 'Aura' }";
        XonObject err2 = (XonObject) new XonParser(new XonLexer(configErr2).tokenize()).parse();
        assertThrows(XonSchemaValidator.SchemaException.class, () -> validator.validate(err2));

        // Invalid: pattern mismatch
        String configErr3 = "{ port: 8080, app: 'Aura123' }";
        XonObject err3 = (XonObject) new XonParser(new XonLexer(configErr3).tokenize()).parse();
        assertThrows(XonSchemaValidator.SchemaException.class, () -> validator.validate(err3));
    }
}

package com.aura.pipeline;

import com.aura.model.ParagraphNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CssParserTest {

    @Test
    public void testCssParsingAndApplying() {
        ParagraphNode root = new ParagraphNode("root", "Header Title");
        ParagraphNode child = new ParagraphNode("intro", "Lead text");
        child.setAttribute("class", "lead-style");
        root.addChild(child);

        String css = "/* stylesheet rules */\n" +
                     "#intro {\n" +
                     "  color: red;\n" +
                     "  font-size: 14px;\n" +
                     "}\n" +
                     ".lead-style {\n" +
                     "  font-weight: bold;\n" +
                     "}";

        CssStyleParser parser = new CssStyleParser();
        parser.parse(css);

        parser.apply(root);

        // Verify applied values
        assertEquals("red", child.getAttribute("color"));
        assertEquals("14px", child.getAttribute("font-size"));
        assertEquals("bold", child.getAttribute("font-weight"));
    }

    @Test
    public void testCssErrors() {
        CssStyleParser parser = new CssStyleParser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("selector { color "));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("selector { color: red"));
    }
}

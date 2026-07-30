package com.aura.pipeline;

import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownImporterTest {

    @Test
    public void testImportBasicMarkdown() {
        MarkdownImporter importer = new MarkdownImporter();
        String md = "This is paragraph 1.\n" +
                    "It spans multiple lines.\n\n" +
                    "![Logo Image](logo.png)\n\n" +
                    "```math\nx + y = z\n```";

        DocumentNode root = importer.importMarkdown(md);
        assertNotNull(root);
        assertEquals("document", root.getNodeType());

        assertEquals(3, root.getChildren().size());

        ParagraphNode p = (ParagraphNode) root.getChildren().get(0);
        assertTrue(p.getText().contains("paragraph 1"));
        assertTrue(p.getText().contains("multiple lines"));

        ImageNode img = (ImageNode) root.getChildren().get(1);
        assertEquals("logo.png", img.getSrc());
        assertEquals("Logo Image", img.getAttribute("alt"));

        ScriptNode script = (ScriptNode) root.getChildren().get(2);
        assertEquals("x + y = z", script.getExpression());
    }

    @Test
    public void testImportTableMarkdown() {
        MarkdownImporter importer = new MarkdownImporter();
        String md = "| Header1 | Header2 |\n" +
                    "| --- | --- |\n" +
                    "| Cell1 | Cell2 |\n" +
                    "| Cell3 | Cell4 |";

        DocumentNode root = importer.importMarkdown(md);
        assertEquals(1, root.getChildren().size());
        
        TableNode tbl = (TableNode) root.getChildren().get(0);
        assertEquals(3, tbl.getRowCount()); // header + 2 data rows
        assertEquals(2, tbl.getColCount());

        ParagraphNode cell0 = (ParagraphNode) tbl.getCell(0, 0);
        assertEquals("Header1", cell0.getText());

        ParagraphNode cell3 = (ParagraphNode) tbl.getCell(2, 1);
        assertEquals("Cell4", cell3.getText());
    }

    @Test
    public void testTableErrors() {
        MarkdownImporter importer = new MarkdownImporter();
        
        // Missing separator row
        String badTable = "| Header1 | Header2 |\n" +
                          "| Cell1 | Cell2 |";
        assertThrows(IllegalArgumentException.class, () -> importer.importMarkdown(badTable));
        
        // Invalid separator
        String badSep = "| Header1 | Header2 |\n" +
                        "| foo | bar |\n" +
                        "| Cell1 | Cell2 |";
        assertThrows(IllegalArgumentException.class, () -> importer.importMarkdown(badSep));
    }
}

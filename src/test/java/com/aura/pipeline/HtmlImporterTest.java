package com.aura.pipeline;

import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.TableNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HtmlImporterTest {

    @Test
    public void testImportBasicHtml() {
        HtmlImporter importer = new HtmlImporter();
        String html = "<article id=\"root\">\n" +
                     "  <p id=\"p1\">Hello World</p>\n" +
                     "  <img id=\"img1\" src=\"icon.png\" width=\"50\" height=\"50\" />\n" +
                     "</article>";

        DocumentNode root = importer.importHtml(html);
        assertNotNull(root);
        assertEquals("article", root.getNodeType());
        assertEquals("root", root.getId());

        assertEquals(2, root.getChildren().size());
        
        ParagraphNode p = (ParagraphNode) root.getChildren().get(0);
        assertEquals("p1", p.getId());
        assertEquals("Hello World", p.getText());

        ImageNode img = (ImageNode) root.getChildren().get(1);
        assertEquals("img1", img.getId());
        assertEquals("icon.png", img.getSrc());
        assertEquals(50, img.getWidth());
        assertEquals(50, img.getHeight());
    }

    @Test
    public void testImportTableHtml() {
        HtmlImporter importer = new HtmlImporter();
        String html = "<table id=\"tbl\" rows=\"2\" cols=\"2\">\n" +
                     "  <tr>\n" +
                     "    <th>Col1</th>\n" +
                     "    <th>Col2</th>\n" +
                     "  </tr>\n" +
                     "  <tr>\n" +
                     "    <td>Val1</td>\n" +
                     "    <td>Val2</td>\n" +
                     "  </tr>\n" +
                     "</table>";

        DocumentNode node = importer.importHtml(html);
        assertTrue(node instanceof TableNode);
        TableNode tbl = (TableNode) node;
        assertEquals(2, tbl.getRowCount());
        assertEquals(2, tbl.getColCount());

        ParagraphNode cell0 = (ParagraphNode) tbl.getCell(0, 0);
        assertEquals("Col1", cell0.getText());
        
        ParagraphNode cell1 = (ParagraphNode) tbl.getCell(1, 1);
        assertEquals("Val2", cell1.getText());
    }

    @Test
    public void testNestingDepthAndMismatchErrors() {
        HtmlImporter importer = new HtmlImporter();
        
        // Mismatched close tag
        assertThrows(IllegalArgumentException.class, () -> importer.importHtml("<div><p>text</div></p>"));
        
        // Orphan close tag
        assertThrows(IllegalArgumentException.class, () -> importer.importHtml("</p>"));
        
        // Malformed tag
        assertThrows(IllegalArgumentException.class, () -> importer.importHtml("<p"));
    }
}

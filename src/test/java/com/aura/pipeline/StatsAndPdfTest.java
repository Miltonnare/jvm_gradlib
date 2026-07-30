package com.aura.pipeline;

import com.aura.model.ParagraphNode;
import com.aura.model.TableNode;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class StatsAndPdfTest {

    @Test
    public void testDocumentStatistics() {
        ParagraphNode root = new ParagraphNode("root", "Root Title with words");
        ParagraphNode child = new ParagraphNode("c1", "More words here");
        root.addChild(child);

        DocumentStatistics stats = new DocumentStatistics();
        Map<String, Object> analysis = stats.analyze(root);

        assertEquals(2, analysis.get("paragraphCount"));
        assertEquals(0, analysis.get("imageCount"));
        assertEquals(7L, analysis.get("wordCount"));
        assertEquals(36L, analysis.get("characterCount"));
    }

    @Test
    public void testPdfMockRenderer() throws IOException {
        LayoutPaginator paginator = new LayoutPaginator(300, 100, 10);
        ParagraphNode p1 = new ParagraphNode("p1", "Test vector layout line");
        List<LayoutPaginator.Page> pages = paginator.paginate(p1);

        PdfMockRenderer pdfRenderer = new PdfMockRenderer();
        byte[] pdfData = pdfRenderer.render(pages);

        assertNotNull(pdfData);
        String pdfText = new String(pdfData, StandardCharsets.US_ASCII);
        assertTrue(pdfText.startsWith("%PDF-MOCK-1.0"));
        assertTrue(pdfText.contains("BT /F1 12 Tf"));
        assertTrue(pdfText.contains("startxref"));
        assertTrue(pdfText.contains("%%EOF"));
    }
}

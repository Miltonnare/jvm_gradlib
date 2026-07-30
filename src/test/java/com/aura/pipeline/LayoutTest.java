package com.aura.pipeline;

import com.aura.model.ParagraphNode;
import com.aura.model.TableNode;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutTest {

    @Test
    public void testPaginatorSplits() {
        // Create a layout paginator with small height to trigger rapid page breaks
        LayoutPaginator paginator = new LayoutPaginator(300, 45, 10);

        ParagraphNode p1 = new ParagraphNode("p1", "This is a very long paragraph that wraps onto multiple lines to trigger page splitting. ".repeat(5));
        List<LayoutPaginator.Page> pages = paginator.paginate(p1);

        assertTrue(pages.size() > 1);
        assertFalse(pages.get(0).getElements().isEmpty());
    }

    @Test
    public void testTableSplittingWithHeaderRepeat() {
        LayoutPaginator paginator = new LayoutPaginator(300, 80, 10); // fits header + 1 row per page

        TableNode tbl = new TableNode("tbl", 4, 2);
        tbl.setCell(0, 0, new ParagraphNode("h0", "Header0"));
        tbl.setCell(0, 1, new ParagraphNode("h1", "Header1"));
        tbl.setCell(1, 0, new ParagraphNode("r1_0", "Row1_0"));
        tbl.setCell(1, 1, new ParagraphNode("r1_1", "Row1_1"));
        tbl.setCell(2, 0, new ParagraphNode("r2_0", "Row2_0"));
        tbl.setCell(2, 1, new ParagraphNode("r2_1", "Row2_1"));
        tbl.setCell(3, 0, new ParagraphNode("r3_0", "Row3_0"));
        tbl.setCell(3, 1, new ParagraphNode("r3_1", "Row3_1"));

        List<LayoutPaginator.Page> pages = paginator.paginate(tbl);

        assertTrue(pages.size() > 1);
        // Verify that the header (ID h0/h1) is repeated on subsequent pages
        boolean hasHeaderOnSubsequentPage = false;
        for (int i = 1; i < pages.size(); i++) {
            for (var el : pages.get(i).getElements()) {
                if ("h0".equals(el.getNode().getId())) {
                    hasHeaderOnSubsequentPage = true;
                    break;
                }
            }
        }
        assertTrue(hasHeaderOnSubsequentPage, "Header h0 should be repeated on new pages");
    }
}

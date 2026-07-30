package com.aura.model;

import com.aura.model.query.QueryExpression;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {

    @Test
    public void testAuraQueryEvaluation() {
        ParagraphNode root = new ParagraphNode("root", "Root Title");
        ParagraphNode p1 = new ParagraphNode("p1", "Intro");
        p1.setAttribute("class", "lead");
        ParagraphNode p2 = new ParagraphNode("p2", "Body content");
        p2.setAttribute("class", "body");
        p2.setAttribute("lang", "en");

        TableNode tbl = new TableNode("tbl1", 2, 2);
        tbl.setCell(0, 0, new ParagraphNode("c1", "Cell Content"));
        tbl.getCell(0, 0).setAttribute("class", "body");

        root.addChild(p1);
        root.addChild(p2);
        root.addChild(tbl);

        // Test direct children query
        QueryExpression q1 = new QueryExpression("/root/paragraph[@class='body']");
        List<DocumentNode> res1 = q1.evaluate(root);
        assertEquals(1, res1.size());
        assertEquals("p2", res1.get(0).getId());

        // Test deep descendant search
        QueryExpression q2 = new QueryExpression("//paragraph[@class='body']");
        List<DocumentNode> res2 = q2.evaluate(root);
        assertEquals(2, res2.size()); // p2 and c1 match class 'body'
        assertEquals("p2", res2.get(0).getId());
        assertEquals("c1", res2.get(1).getId());

        // Test index filter selection
        QueryExpression q3 = new QueryExpression("//paragraph[@class='body'][1]");
        List<DocumentNode> res3 = q3.evaluate(root);
        assertEquals(1, res3.size());
        assertEquals("c1", res3.get(0).getId()); // second match is index 1

        // Test attribute existence check
        QueryExpression q4 = new QueryExpression("/root/paragraph[@lang]");
        List<DocumentNode> res4 = q4.evaluate(root);
        assertEquals(1, res4.size());
        assertEquals("p2", res4.get(0).getId());
    }

    @Test
    public void testQueryParserErrors() {
        assertThrows(IllegalArgumentException.class, () -> new QueryExpression("/root/"));
        assertThrows(IllegalArgumentException.class, () -> new QueryExpression("/root//"));
        assertThrows(IllegalArgumentException.class, () -> new QueryExpression("paragraph[invalid]"));
    }
}

package com.aura.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    public void testDocumentNodes() {
        ParagraphNode p = new ParagraphNode("p1", "Welcome to Aura");
        assertEquals("paragraph", p.getNodeType());
        assertEquals("Welcome to Aura", p.getText());

        ImageNode img = new ImageNode("img1", "logo.png", 100, 20);
        assertEquals("image", img.getNodeType());
        assertEquals("logo.png", img.getSrc());
        assertEquals(100, img.getWidth());

        TableNode tbl = new TableNode("tbl1", 2, 2);
        assertEquals("table", tbl.getNodeType());
        tbl.setCell(0, 0, new ParagraphNode("c1", "Header 1"));
        assertEquals("c1", tbl.getCell(0, 0).getId());
    }

    @Test
    public void testScriptEvaluator() {
        ScriptNode s = new ScriptNode("s1", "(2 * val + 10) / offset");
        Map<String, Double> ctx = new HashMap<>();
        ctx.put("val", 15.0);
        ctx.put("offset", 5.0);

        double result = s.evaluate(ctx);
        assertEquals(8.0, result, 0.0001);

        ScriptNode divZero = new ScriptNode("s2", "10 / 0");
        assertThrows(ArithmeticException.class, () -> divZero.evaluate(ctx));
    }

    @Test
    public void testDocumentGraphAndCycleDetection() {
        ParagraphNode root = new ParagraphNode("root", "Root text");
        ParagraphNode child1 = new ParagraphNode("c1", "Child 1");
        ParagraphNode child2 = new ParagraphNode("c2", "Child 2");

        root.addChild(child1);
        root.addChild(child2);

        // Add attributes pointing to targets (no cycle yet)
        child1.setAttribute("ref", "c2");
        child2.setAttribute("ref", "other");

        DocumentGraph graph = new DocumentGraph(root);
        assertFalse(graph.hasCircularReferences());

        // Create cyclic references in attributes: child2 -> child1
        child2.setAttribute("ref", "c1");
        DocumentGraph cyclicGraph = new DocumentGraph(root);
        assertTrue(cyclicGraph.hasCircularReferences());
    }

    @Test
    public void testDocumentQuery() {
        ParagraphNode root = new ParagraphNode("root", "Root");
        ParagraphNode p1 = new ParagraphNode("p1", "Intro Paragraph");
        p1.setAttribute("class", "lead");
        ImageNode img = new ImageNode("img1", "icon.png", 50, 50);
        img.setAttribute("class", "icon");

        root.addChild(p1);
        root.addChild(img);

        DocumentQuery q1 = new DocumentQuery("/root/paragraph[@class='lead']");
        List<DocumentNode> res1 = q1.select(root);
        assertEquals(1, res1.size());
        assertEquals("p1", res1.get(0).getId());

        DocumentQuery q2 = new DocumentQuery("/*/image[@class='icon']");
        List<DocumentNode> res2 = q2.select(root);
        assertEquals(1, res2.size());
        assertEquals("img1", res2.get(0).getId());
    }
}

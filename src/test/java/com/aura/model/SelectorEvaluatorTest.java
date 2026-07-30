package com.aura.model;

import com.aura.model.query.SelectorEvaluator;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SelectorEvaluatorTest {

    @Test
    public void testSiblingEvaluations() {
        ParagraphNode root = new ParagraphNode("root", "Header");
        ParagraphNode p1 = new ParagraphNode("p1", "Intro");
        ImageNode img = new ImageNode("img", "icon.png", 10, 10);
        ParagraphNode p2 = new ParagraphNode("p2", "Body");

        root.addChild(p1);
        root.addChild(img);
        root.addChild(p2);

        // p1 sibling of type paragraph
        List<DocumentNode> sibs = SelectorEvaluator.findSiblings(p1, "paragraph");
        assertEquals(1, sibs.size());
        assertEquals("p2", sibs.get(0).getId());

        // Sibling index
        assertEquals(0, SelectorEvaluator.getSiblingIndex(p1));
        assertEquals(1, SelectorEvaluator.getSiblingIndex(p2)); // second paragraph is index 1

        // Has child of type image
        assertTrue(SelectorEvaluator.hasChildOfType(root, "image"));
        assertFalse(SelectorEvaluator.hasChildOfType(p1, "image"));
    }
}

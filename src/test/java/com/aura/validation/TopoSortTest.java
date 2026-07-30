package com.aura.validation;

import com.aura.model.ParagraphNode;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TopoSortTest {

    @Test
    public void testTopologicalSortOrder() throws ValidationException {
        ParagraphNode root = new ParagraphNode("root", "Root Node");
        ParagraphNode child1 = new ParagraphNode("c1", "Child 1");
        ParagraphNode child2 = new ParagraphNode("c2", "Child 2");
        root.addChild(child1);
        root.addChild(child2);

        DocumentTopologicalSort sorter = new DocumentTopologicalSort();
        List<String> order = sorter.sort(root);

        assertNotNull(order);
        assertEquals(3, order.size());
        assertEquals("root", order.get(0));
        assertTrue(order.contains("c1"));
        assertTrue(order.contains("c2"));
    }

    @Test
    public void testTopoCycleException() {
        ParagraphNode root = new ParagraphNode("root", "Root Node");
        ParagraphNode child1 = new ParagraphNode("c1", "Child 1");
        root.addChild(child1);
        
        // Form cyclic reference using custom backlink addition
        child1.addChild(root);

        DocumentTopologicalSort sorter = new DocumentTopologicalSort();
        assertThrows(ValidationException.class, () -> sorter.sort(root));
    }
}

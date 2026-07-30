package com.aura.validation;

import com.aura.model.DocumentNode;
import com.aura.model.ParagraphNode;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class DiffTest {

    @Test
    public void testDiffAndPatch() {
        ParagraphNode origRoot = new ParagraphNode("root", "Root Title");
        ParagraphNode child1 = new ParagraphNode("c1", "First Child");
        ParagraphNode child2 = new ParagraphNode("c2", "Second Child");
        origRoot.addChild(child1);
        origRoot.addChild(child2);

        // Modify text of child1, delete child2, insert child3
        ParagraphNode revRoot = new ParagraphNode("root", "Root Title");
        ParagraphNode revChild1 = new ParagraphNode("c1", "Modified First Child");
        ParagraphNode revChild3 = new ParagraphNode("c3", "Third Child");
        revRoot.addChild(revChild1);
        revRoot.addChild(revChild3);

        DocumentDiffEngine diffEngine = new DocumentDiffEngine();
        List<DocumentDiffEngine.DiffOp> ops = diffEngine.diff(origRoot, revRoot);

        assertEquals(3, ops.size());

        // Apply patch to original tree
        DocumentPatchEngine patchEngine = new DocumentPatchEngine();
        DocumentNode patched = patchEngine.patch(origRoot, ops);

        assertNotNull(patched);
        assertEquals(2, patched.getChildren().size());
        
        ParagraphNode p1 = (ParagraphNode) patched.getChildren().get(0);
        assertEquals("c1", p1.getId());
        assertEquals("Modified First Child", p1.getText());

        ParagraphNode p3 = (ParagraphNode) patched.getChildren().get(1);
        assertEquals("c3", p3.getId());
        assertEquals("Third Child", p3.getText());
    }
}

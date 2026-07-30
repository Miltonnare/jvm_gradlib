package com.aura.pipeline;

import com.aura.model.DocumentNode;
import com.aura.model.ParagraphNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineExecutorTest {

    @Test
    public void testSequentialPipelineExecution() {
        StageExecutor executor = new StageExecutor();
        
        // Mock stage that appends text to a ParagraphNode
        executor.addStage(input -> {
            ParagraphNode p = (ParagraphNode) input;
            p.setText(p.getText() + " World");
            return p;
        });

        ParagraphNode root = new ParagraphNode("id", "Hello");
        ParagraphNode result = (ParagraphNode) executor.execute(root);

        assertEquals("Hello World", result.getText());
        
        StageContext context = executor.getContext();
        assertNotNull(context);
        assertFalse(context.getPerformanceTimings().isEmpty());
    }
}

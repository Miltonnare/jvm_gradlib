package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;

/**
 * Pipeline stage or utility to extract all textual content from a document graph.
 */
public class TextExtractor implements DocumentNode.DocumentVisitor, TransformationPipeline.Stage<DocumentNode, String> {
    private final StringBuilder buffer = new StringBuilder();

    public String extract(DocumentNode node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        buffer.setLength(0);
        traverse(node);
        return buffer.toString().trim();
    }

    private void traverse(DocumentNode node) {
        node.accept(this);
        for (DocumentNode child : node.getChildren()) {
            traverse(child);
        }
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        buffer.append(paragraph.getText()).append("\n\n");
    }

    @Override
    public void visit(ImageNode image) {
        // Images do not contain direct textual body content, but we can extract alternative description if set
        String alt = image.getAttribute("alt");
        if (alt != null && !alt.isEmpty()) {
            buffer.append("[Image: ").append(alt).append("]\n\n");
        }
    }

    @Override
    public void visit(TableNode table) {
        // Tables contain nested cells which will be traversed recursively,
        // but we can add grid formatting markers.
        buffer.append("[Table Grid ").append(table.getRowCount()).append("x").append(table.getColCount()).append("]\n");
    }

    @Override
    public void visit(ScriptNode script) {
        // Inline scripts are executable expressions, we can output their expression string
        buffer.append("[Script: ").append(script.getExpression()).append("]\n\n");
    }

    @Override
    public String process(DocumentNode input) {
        return extract(input);
    }
}

package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;

/**
 * Renders a document graph to GitHub Flavored Markdown (GFM).
 */
public class MarkdownRenderer implements DocumentNode.DocumentVisitor, TransformationPipeline.Stage<DocumentNode, String> {
    private final StringBuilder sb = new StringBuilder();

    public String render(DocumentNode node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        sb.setLength(0);
        traverse(node);
        return sb.toString();
    }

    private void traverse(DocumentNode node) {
        node.accept(this);
        if (!(node instanceof TableNode)) {
            for (DocumentNode child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        sb.append(paragraph.getText()).append("\n\n");
    }

    @Override
    public void visit(ImageNode image) {
        String alt = image.getAttribute("alt");
        if (alt == null || alt.isEmpty()) {
            alt = image.getId();
        }
        sb.append("![")
          .append(alt)
          .append("](")
          .append(image.getSrc())
          .append(")\n\n");
    }

    @Override
    public void visit(TableNode table) {
        // Render Markdown Table
        int rows = table.getRowCount();
        int cols = table.getColCount();

        for (int r = 0; r < rows; r++) {
            sb.append("| ");
            for (int c = 0; c < cols; c++) {
                DocumentNode cell = table.getCell(r, c);
                if (cell != null) {
                    // Extract text inline
                    String cellText = new TextExtractor().extract(cell).replace("\n", " ");
                    sb.append(cellText);
                }
                sb.append(" | ");
            }
            sb.append("\n");

            // Add separator row after header
            if (r == 0) {
                sb.append("|");
                for (int c = 0; c < cols; c++) {
                    sb.append(" --- |");
                }
                sb.append("\n");
            }
        }
        sb.append("\n");
    }

    @Override
    public void visit(ScriptNode script) {
        sb.append("```math\n")
          .append(script.getExpression())
          .append("\n```\n\n");
    }

    @Override
    public String process(DocumentNode input) {
        return render(input);
    }
}

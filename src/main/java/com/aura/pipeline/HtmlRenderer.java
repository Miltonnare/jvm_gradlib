package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;

/**
 * Renders a document graph to semantic HTML5 content.
 */
public class HtmlRenderer implements DocumentNode.DocumentVisitor, TransformationPipeline.Stage<DocumentNode, String> {
    private final StringBuilder sb = new StringBuilder();

    public String render(DocumentNode node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        sb.setLength(0);
        sb.append("<article class=\"aura-document\" id=\"").append(node.getId()).append("\">\n");
        traverse(node);
        sb.append("</article>\n");
        return sb.toString();
    }

    private void traverse(DocumentNode node) {
        // Only call accept on the node if it's not the root generic document wrapper (to avoid empty loops),
        // or call accept universally since structural nodes are no-ops.
        node.accept(this);
        if (!(node instanceof TableNode)) {
            for (DocumentNode child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        sb.append("  <p id=\"").append(paragraph.getId()).append("\">")
          .append(escapeHtml(paragraph.getText()))
          .append("</p>\n");
    }

    @Override
    public void visit(ImageNode image) {
        sb.append("  <img id=\"").append(image.getId()).append("\" src=\"")
          .append(escapeHtml(image.getSrc())).append("\" width=\"")
          .append(image.getWidth()).append("\" height=\"")
          .append(image.getHeight()).append("\"");
        
        String alt = image.getAttribute("alt");
        if (alt != null) {
            sb.append(" alt=\"").append(escapeHtml(alt)).append("\"");
        }
        sb.append(" />\n");
    }

    @Override
    public void visit(TableNode table) {
        sb.append("  <table id=\"").append(table.getId()).append("\">\n");
        int rows = table.getRowCount();
        int cols = table.getColCount();

        for (int r = 0; r < rows; r++) {
            sb.append("    <tr>\n");
            for (int c = 0; c < cols; c++) {
                String tag = (r == 0) ? "th" : "td";
                sb.append("      <").append(tag).append(">");
                DocumentNode cell = table.getCell(r, c);
                if (cell != null) {
                    // Render cell content inline
                    cell.accept(this);
                }
                sb.append("</").append(tag).append(">\n");
            }
            sb.append("    </tr>\n");
        }
        sb.append("  </table>\n");
    }

    @Override
    public void visit(ScriptNode script) {
        sb.append("  <code class=\"aura-script\" id=\"").append(script.getId()).append("\">")
          .append(escapeHtml(script.getExpression()))
          .append("</code>\n");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    @Override
    public String process(DocumentNode input) {
        return render(input);
    }
}

package com.aura.validation;

import com.aura.model.DocumentGraph;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;

/**
 * Validation rule verifying structural integrity constraints on individual nodes.
 */
public class IntegrityRule implements ValidationRule, DocumentNode.DocumentVisitor {

    @Override
    public void validate(DocumentGraph graph) throws ValidationException {
        for (DocumentNode node : graph.getNodeIndex().values()) {
            node.accept(this);
        }
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        if (paragraph.getId().trim().isEmpty()) {
            throw new ValidationException("Paragraph ID cannot be empty");
        }
        if (paragraph.getText() == null) {
            throw new ValidationException("Paragraph text content cannot be null");
        }
    }

    @Override
    public void visit(ImageNode image) {
        if (image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new ValidationException(String.format("Image '%s' has invalid dimensions: %dx%d",
                image.getId(), image.getWidth(), image.getHeight()));
        }
        if (image.getSrc() == null || image.getSrc().trim().isEmpty()) {
            throw new ValidationException("Image source URI cannot be empty");
        }
    }

    @Override
    public void visit(TableNode table) {
        if (table.getRowCount() <= 0 || table.getColCount() <= 0) {
            throw new ValidationException("Table rows and columns must be greater than zero");
        }
        // Verify all cells are within row/col counts
        for (int r = 0; r < table.getRowCount(); r++) {
            for (int c = 0; c < table.getColCount(); c++) {
                DocumentNode cell = table.getCell(r, c);
                if (cell != null && cell.getParent() != table) {
                    throw new ValidationException(String.format("Table '%s' cell [%d,%d] parent is incorrect",
                        table.getId(), r, c));
                }
            }
        }
    }

    @Override
    public void visit(ScriptNode script) {
        if (script.getExpression() == null || script.getExpression().trim().isEmpty()) {
            throw new ValidationException("Script expression cannot be empty");
        }
    }

    @Override
    public String getName() {
        return "IntegrityRule";
    }
}

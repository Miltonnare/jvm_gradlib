package com.aura.model;

import com.aura.internal.Preconditions;

/**
 * Node representing text paragraph content.
 */
public class ParagraphNode extends DocumentNode {
    private String text;

    public ParagraphNode(String id, String text) {
        super(id);
        this.text = Preconditions.checkNotNull(text, "Paragraph text cannot be null");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = Preconditions.checkNotNull(text, "Text cannot be null");
    }

    @Override
    public String getNodeType() {
        return "paragraph";
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }
}

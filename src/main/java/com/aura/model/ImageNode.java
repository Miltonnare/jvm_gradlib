package com.aura.model;

import com.aura.internal.Preconditions;

/**
 * Node representing an image entry with size dimensions and source URI.
 */
public class ImageNode extends DocumentNode {
    private String src;
    private int width;
    private int height;

    public ImageNode(String id, String src, int width, int height) {
        super(id);
        this.src = Preconditions.checkNotNull(src, "Source cannot be null");
        Preconditions.checkArgument(width >= 0, "Width cannot be negative");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative");
        this.width = width;
        this.height = height;
    }

    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = Preconditions.checkNotNull(src, "Src cannot be null"); }

    public int getWidth() { return width; }
    public void setWidth(int width) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative");
        this.width = width;
    }

    public int getHeight() { return height; }
    public void setHeight(int height) {
        Preconditions.checkArgument(height >= 0, "Height cannot be negative");
        this.height = height;
    }

    @Override
    public String getNodeType() {
        return "image";
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }
}

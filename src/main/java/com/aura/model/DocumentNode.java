package com.aura.model;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base abstract class for nodes in the Document Graph.
 */
public abstract class DocumentNode {
    private final String id;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<DocumentNode> children = new ArrayList<>();
    private DocumentNode parent;

    protected DocumentNode(String id) {
        this.id = Preconditions.checkNotNull(id, "Node ID cannot be null");
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String key, String value) {
        Preconditions.checkNotNull(key, "Attribute key cannot be null");
        Preconditions.checkNotNull(value, "Attribute value cannot be null");
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void addChild(DocumentNode child) {
        Preconditions.checkNotNull(child, "Child node cannot be null");
        Preconditions.checkArgument(child != this, "Cannot add node as a child of itself");
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        children.add(child);
    }

    public void removeChild(DocumentNode child) {
        Preconditions.checkNotNull(child, "Child node cannot be null");
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    public DocumentNode getParent() {
        return parent;
    }

    public List<DocumentNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public abstract String getNodeType();

    public abstract void accept(DocumentVisitor visitor);

    public interface DocumentVisitor {
        void visit(ParagraphNode paragraph);
        void visit(ImageNode image);
        void visit(TableNode table);
        void visit(ScriptNode script);
    }
}

package com.aura.validation;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ParagraphNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies a list of DiffOp modifications to reconstruct the revised DocumentNode graph.
 */
public class DocumentPatchEngine {

    /**
     * Applies the diff operations onto the original root node and returns the root.
     */
    public DocumentNode patch(DocumentNode originalRoot, List<DocumentDiffEngine.DiffOp> ops) {
        Preconditions.checkNotNull(originalRoot, "Original root cannot be null");
        Preconditions.checkNotNull(ops, "Ops list cannot be null");

        // 1. Index tree nodes by ID to allow instant lookups and updates
        Map<String, DocumentNode> index = new HashMap<>();
        indexTree(originalRoot, index);

        for (DocumentDiffEngine.DiffOp op : ops) {
            String nodeId = op.getNodeId();
            
            switch (op.getType()) {
                case DELETE_NODE -> {
                    DocumentNode target = index.get(nodeId);
                    if (target != null) {
                        DocumentNode parent = findParent(originalRoot, nodeId);
                        if (parent != null) {
                            parent.removeChild(target);
                        }
                        // Remove from index
                        removeFromIndex(target, index);
                    }
                }
                case INSERT_NODE -> {
                    String pId = op.getParentId();
                    DocumentNode parent = (pId != null) ? index.get(pId) : originalRoot;
                    DocumentNode element = op.getElement();
                    if (parent != null && element != null) {
                        // Add node to parent
                        parent.addChild(element);
                        // Add elements to index recursively
                        indexTree(element, index);
                    }
                }
                case MODIFY_ATTR -> {
                    DocumentNode target = index.get(nodeId);
                    if (target != null) {
                        String key = op.getKey();
                        String newVal = op.getNewValue();
                        if (newVal == null) {
                            target.getAttributes().remove(key);
                        } else {
                            target.setAttribute(key, newVal);
                        }
                    }
                }
                case MODIFY_TEXT -> {
                    DocumentNode target = index.get(nodeId);
                    if (target instanceof ParagraphNode p) {
                        p.setText(op.getNewValue());
                    }
                }
            }
        }

        return originalRoot;
    }

    private void indexTree(DocumentNode node, Map<String, DocumentNode> map) {
        if (node == null) return;
        map.put(node.getId(), node);
        for (DocumentNode child : node.getChildren()) {
            indexTree(child, map);
        }
    }

    private void removeFromIndex(DocumentNode node, Map<String, DocumentNode> map) {
        if (node == null) return;
        map.remove(node.getId());
        for (DocumentNode child : node.getChildren()) {
            removeFromIndex(child, map);
        }
    }

    private DocumentNode findParent(DocumentNode root, String targetId) {
        if (root == null) return null;
        for (DocumentNode child : root.getChildren()) {
            if (child.getId().equals(targetId)) {
                return root;
            }
            DocumentNode p = findParent(child, targetId);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
}

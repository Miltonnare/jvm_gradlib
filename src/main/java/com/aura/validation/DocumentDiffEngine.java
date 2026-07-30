package com.aura.validation;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ParagraphNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes structural and property differences between two DocumentNode trees.
 */
public class DocumentDiffEngine {

    public enum OpType {
        INSERT_NODE,
        DELETE_NODE,
        MODIFY_ATTR,
        MODIFY_TEXT
    }

    public static class DiffOp {
        private final OpType type;
        private final String nodeId;
        private final String key;
        private final String oldValue;
        private final String newValue;
        private final String parentId;
        private final DocumentNode element;

        public DiffOp(OpType type, String nodeId, String key, String oldValue, String newValue, DocumentNode element) {
            this(type, nodeId, key, oldValue, newValue, null, element);
        }

        public DiffOp(OpType type, String nodeId, String key, String oldValue, String newValue, String parentId, DocumentNode element) {
            this.type = type;
            this.nodeId = nodeId;
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.parentId = parentId;
            this.element = element;
        }

        public OpType getType() { return type; }
        public String getNodeId() { return nodeId; }
        public String getKey() { return key; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }
        public String getParentId() { return parentId; }
        public DocumentNode getElement() { return element; }

        @Override
        public String toString() {
            return String.format("DiffOp(%s, id=%s, key=%s, old=%s, new=%s, parent=%s)", type, nodeId, key, oldValue, newValue, parentId);
        }
    }

    /**
     * Compares the original tree with the revised tree and returns a list of difference operations.
     */
    public List<DiffOp> diff(DocumentNode original, DocumentNode revised) {
        Preconditions.checkNotNull(original, "Original node cannot be null");
        Preconditions.checkNotNull(revised, "Revised node cannot be null");

        List<DiffOp> ops = new ArrayList<>();
        Map<String, DocumentNode> originalMap = indexTree(original);
        Map<String, DocumentNode> revisedMap = indexTree(revised);

        // 1. Detect Deleted Nodes
        for (String id : originalMap.keySet()) {
            if (!revisedMap.containsKey(id)) {
                ops.add(new DiffOp(OpType.DELETE_NODE, id, null, null, null, originalMap.get(id)));
            }
        }

        // 2. Detect Inserted or Modified Nodes
        for (Map.Entry<String, DocumentNode> entry : revisedMap.entrySet()) {
            String id = entry.getKey();
            DocumentNode revNode = entry.getValue();

            if (!originalMap.containsKey(id)) {
                // Inserted - find parent ID in revised tree
                String pId = findParentId(revised, id);
                ops.add(new DiffOp(OpType.INSERT_NODE, id, null, null, null, pId, revNode));
            } else {
                // Node exists in both, check modifications
                DocumentNode origNode = originalMap.get(id);
                diffProperties(origNode, revNode, ops);
            }
        }

        return ops;
    }

    private void diffProperties(DocumentNode orig, DocumentNode rev, List<DiffOp> ops) {
        // Compare type
        if (!orig.getNodeType().equalsIgnoreCase(rev.getNodeType())) {
            ops.add(new DiffOp(OpType.MODIFY_ATTR, orig.getId(), "nodeType", orig.getNodeType(), rev.getNodeType(), rev));
        }

        // Compare attributes
        Set<String> allKeys = new HashSet<>();
        orig.getAttributes().keySet().forEach(k -> allKeys.add(k.toLowerCase()));
        rev.getAttributes().keySet().forEach(k -> allKeys.add(k.toLowerCase()));

        for (String key : allKeys) {
            String origVal = orig.getAttribute(key);
            String revVal = rev.getAttribute(key);

            if (origVal != null && revVal == null) {
                ops.add(new DiffOp(OpType.MODIFY_ATTR, orig.getId(), key, origVal, null, rev));
            } else if (origVal == null && revVal != null) {
                ops.add(new DiffOp(OpType.MODIFY_ATTR, orig.getId(), key, null, revVal, rev));
            } else if (origVal != null && !origVal.equals(revVal)) {
                ops.add(new DiffOp(OpType.MODIFY_ATTR, orig.getId(), key, origVal, revVal, rev));
            }
        }

        // Compare text if paragraphs
        if (orig instanceof ParagraphNode origP && rev instanceof ParagraphNode revP) {
            if (!origP.getText().equals(revP.getText())) {
                ops.add(new DiffOp(OpType.MODIFY_TEXT, orig.getId(), "text", origP.getText(), revP.getText(), rev));
            }
        }
    }

    private Map<String, DocumentNode> indexTree(DocumentNode node) {
        Map<String, DocumentNode> map = new HashMap<>();
        indexNode(node, map);
        return map;
    }

    private void indexNode(DocumentNode node, Map<String, DocumentNode> map) {
        if (node == null) return;
        map.put(node.getId(), node);
        for (DocumentNode child : node.getChildren()) {
            indexNode(child, map);
        }
    }

    private String findParentId(DocumentNode root, String targetId) {
        if (root == null) return null;
        for (DocumentNode child : root.getChildren()) {
            if (child.getId().equals(targetId)) {
                return root.getId();
            }
            String pId = findParentId(child, targetId);
            if (pId != null) {
                return pId;
            }
        }
        return null;
    }
}

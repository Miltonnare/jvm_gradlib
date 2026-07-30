package com.aura.model.query;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility helper to evaluate tree selectors, match sibling nodes, and query sibling counts.
 */
public class SelectorEvaluator {

    /**
     * Finds sibling elements of the target node matching the specific type.
     */
    public static List<DocumentNode> findSiblings(DocumentNode node, String nodeType) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        Preconditions.checkNotNull(nodeType, "Node type cannot be null");

        DocumentNode parent = node.getParent();
        if (parent == null) {
            return Collections.emptyList();
        }

        List<DocumentNode> siblings = new ArrayList<>();
        for (DocumentNode child : parent.getChildren()) {
            if (child != node && (nodeType.equals("*") || child.getNodeType().equalsIgnoreCase(nodeType))) {
                siblings.add(child);
            }
        }
        return siblings;
    }

    /**
     * Determines the zero-based index of the node among siblings of the same node type.
     */
    public static int getSiblingIndex(DocumentNode node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        DocumentNode parent = node.getParent();
        if (parent == null) {
            return 0;
        }

        String type = node.getNodeType();
        int idx = 0;
        for (DocumentNode child : parent.getChildren()) {
            if (child == node) {
                return idx;
            }
            if (child.getNodeType().equalsIgnoreCase(type)) {
                idx++;
            }
        }
        return -1;
    }

    /**
     * Evaluates if a node has children of a specific type.
     */
    public static boolean hasChildOfType(DocumentNode node, String nodeType) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        Preconditions.checkNotNull(nodeType, "Node type cannot be null");
        
        for (DocumentNode child : node.getChildren()) {
            if (nodeType.equals("*") || child.getNodeType().equalsIgnoreCase(nodeType)) {
                return true;
            }
        }
        return false;
    }
}

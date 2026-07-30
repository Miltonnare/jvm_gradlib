package com.aura.model;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the index and reference graph of all document nodes in a document structure.
 */
public class DocumentGraph {
    private final DocumentNode root;
    private final Map<String, DocumentNode> nodeIndex = new LinkedHashMap<>();
    private final Map<String, List<String>> referenceAdjacency = new HashMap<>();

    public DocumentGraph(DocumentNode root) {
        this.root = Preconditions.checkNotNull(root, "Root node cannot be null");
        indexNode(root);
        buildReferenceGraph();
    }

    public DocumentNode getRoot() {
        return root;
    }

    public DocumentNode getNode(String id) {
        return nodeIndex.get(id);
    }

    public Map<String, DocumentNode> getNodeIndex() {
        return Collections.unmodifiableMap(nodeIndex);
    }

    public List<String> getReferences(String id) {
        return referenceAdjacency.getOrDefault(id, Collections.emptyList());
    }

    private void indexNode(DocumentNode node) {
        if (nodeIndex.containsKey(node.getId())) {
            throw new IllegalArgumentException("Duplicate node ID detected in graph: " + node.getId());
        }
        nodeIndex.put(node.getId(), node);
        for (DocumentNode child : node.getChildren()) {
            indexNode(child);
        }
    }

    private void buildReferenceGraph() {
        for (DocumentNode node : nodeIndex.values()) {
            String refsAttr = node.getAttribute("ref");
            if (refsAttr != null && !refsAttr.isEmpty()) {
                String[] targets = refsAttr.split(",");
                List<String> validTargets = new ArrayList<>();
                for (String t : targets) {
                    String targetId = t.trim();
                    if (!targetId.isEmpty()) {
                        validTargets.add(targetId);
                    }
                }
                if (!validTargets.isEmpty()) {
                    referenceAdjacency.put(node.getId(), validTargets);
                }
            }
        }
    }

    /**
     * Checks if there are any circular references in node attributes.
     * Uses Depth-First Search for cycle detection.
     */
    public boolean hasCircularReferences() {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        for (String nodeId : nodeIndex.keySet()) {
            if (checkCycleDfs(nodeId, visited, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCycleDfs(String current, Set<String> visited, Set<String> stack) {
        if (stack.contains(current)) {
            return true; // Cycle detected
        }
        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        stack.add(current);

        List<String> neighbors = referenceAdjacency.get(current);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (checkCycleDfs(neighbor, visited, stack)) {
                    return true;
                }
            }
        }

        stack.remove(current);
        return false;
    }
}

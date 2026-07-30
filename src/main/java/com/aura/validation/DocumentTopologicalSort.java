package com.aura.validation;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes a topological sort on the Document Graph to resolve dependency order
 * and verify the absence of cycle constraints.
 */
public class DocumentTopologicalSort {

    /**
     * Performs a topological sort on the document graph and returns a list of sorted node IDs.
     * Throws ValidationException if a cycle is detected.
     */
    public List<String> sort(DocumentNode root) throws ValidationException {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        // Map parent-child connections
        Map<String, List<String>> adj = new HashMap<>();
        buildAdjacencyList(root, adj, new HashSet<>());

        // Sort starting from root
        dfs(root.getId(), adj, visited, visiting, order);

        // Reverse to get correct topological order (dependencies first)
        Collections.reverse(order);
        return order;
    }

    private void dfs(String node, Map<String, List<String>> adj, Set<String> visited, Set<String> visiting, List<String> order) throws ValidationException {
        if (visiting.contains(node)) {
            throw new ValidationException("Dependency cycle detected in graph containing node: " + node);
        }
        if (visited.contains(node)) {
            return;
        }

        visiting.add(node);
        List<String> children = adj.getOrDefault(node, Collections.emptyList());
        for (String child : children) {
            dfs(child, adj, visited, visiting, order);
        }

        visiting.remove(node);
        visited.add(node);
        order.add(node);
    }

    private void buildAdjacencyList(DocumentNode node, Map<String, List<String>> adj, Set<String> visited) {
        if (node == null || visited.contains(node.getId())) return;
        visited.add(node.getId());
        List<String> childrenIds = new ArrayList<>();
        for (DocumentNode child : node.getChildren()) {
            childrenIds.add(child.getId());
            buildAdjacencyList(child, adj, visited);
        }
        adj.put(node.getId(), childrenIds);
    }
}

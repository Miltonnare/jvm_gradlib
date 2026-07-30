package com.aura.model.query;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Compiler and execution context for path queries on the Document Node Graph.
 */
public class QueryExpression {
    private final List<SelectorNode> steps;
    private final boolean absolute;

    public QueryExpression(String expression) {
        Preconditions.checkNotNull(expression, "Expression string cannot be null");
        QueryLexer lexer = new QueryLexer(expression);
        QueryParser parser = new QueryParser(lexer.tokenize());
        this.steps = parser.parse();
        this.absolute = parser.isAbsolute();
    }

    /**
     * Executes the compiled path query on the given root node.
     */
    public List<DocumentNode> evaluate(DocumentNode root) {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        if (steps.isEmpty()) {
            return Collections.emptyList();
        }

        List<DocumentNode> currentCandidates = new ArrayList<>();
        currentCandidates.add(root);

        int startIdx = 0;
        if (absolute && !steps.isEmpty()) {
            SelectorNode firstStep = steps.get(0);
            if (firstStep.matchesNode(root)) {
                List<DocumentNode> rootList = new ArrayList<>();
                rootList.add(root);
                currentCandidates = applyFilters(rootList, firstStep);
                startIdx = 1;
            } else {
                return Collections.emptyList();
            }
        }

        for (int i = startIdx; i < steps.size(); i++) {
            currentCandidates = evaluateStep(currentCandidates, steps.get(i));
            if (currentCandidates.isEmpty()) {
                break;
            }
        }

        return currentCandidates;
    }

    private List<DocumentNode> applyFilters(List<DocumentNode> result, SelectorNode step) {
        List<DocumentNode> filtered = new ArrayList<>(result);
        for (SelectorNode.Filter filter : step.getFilters()) {
            if (filter instanceof SelectorNode.IndexFilter idxFilter) {
                int idx = idxFilter.getIndex();
                if (idx >= 0 && idx < filtered.size()) {
                    DocumentNode targeted = filtered.get(idx);
                    filtered.clear();
                    filtered.add(targeted);
                } else {
                    return Collections.emptyList();
                }
            }
        }
        return filtered;
    }

    private List<DocumentNode> evaluateStep(List<DocumentNode> candidates, SelectorNode step) {
        Set<DocumentNode> matched = new LinkedHashSet<>();

        for (DocumentNode node : candidates) {
            if (step.isDeep()) {
                // Search recursively all descendants including the node itself (optional)
                searchDescendants(node, step, matched);
            } else {
                // Search direct children of the node
                for (DocumentNode child : node.getChildren()) {
                    if (step.matchesNode(child)) {
                        matched.add(child);
                    }
                }
            }
        }

        List<DocumentNode> result = new ArrayList<>(matched);

        // Apply index filters if any
        for (SelectorNode.Filter filter : step.getFilters()) {
            if (filter instanceof SelectorNode.IndexFilter idxFilter) {
                int idx = idxFilter.getIndex();
                if (idx >= 0 && idx < result.size()) {
                    DocumentNode targeted = result.get(idx);
                    result.clear();
                    result.add(targeted);
                } else {
                    return Collections.emptyList();
                }
            }
        }

        return result;
    }

    private void searchDescendants(DocumentNode node, SelectorNode step, Set<DocumentNode> result) {
        if (step.matchesNode(node)) {
            result.add(node);
        }
        for (DocumentNode child : node.getChildren()) {
            searchDescendants(child, step, result);
        }
    }
}

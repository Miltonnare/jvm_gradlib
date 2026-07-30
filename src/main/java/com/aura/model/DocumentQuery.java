package com.aura.model;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A basic document tree searcher supporting path query selectors.
 * Example path: "/document/paragraph[@id='intro']" or "/table/cell[@col='0']".
 */
public class DocumentQuery {
    private final String query;

    public DocumentQuery(String query) {
        this.query = Preconditions.checkNotNull(query, "Query string cannot be null");
    }

    /**
     * Executes the query path search on a starting root node.
     */
    public List<DocumentNode> select(DocumentNode root) {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        if (query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String cleaned = query.trim();
        boolean absolute = cleaned.startsWith("/");
        if (absolute) {
            cleaned = cleaned.substring(1);
        }

        String[] segments = cleaned.split("/");
        List<DocumentNode> currentCandidates = new ArrayList<>();
        currentCandidates.add(root);

        int startIdx = 0;
        if (absolute && segments.length > 0 && !segments[0].isEmpty()) {
            String rootSegment = segments[0];
            List<DocumentNode> rootMatch = evaluateSegmentSelf(root, rootSegment);
            if (rootMatch.isEmpty()) {
                return Collections.emptyList();
            }
            currentCandidates = rootMatch;
            startIdx = 1;
        }

        for (int i = startIdx; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) {
                continue;
            }
            currentCandidates = evaluateSegment(currentCandidates, segment);
            if (currentCandidates.isEmpty()) {
                break;
            }
        }

        return currentCandidates;
    }

    private List<DocumentNode> evaluateSegmentSelf(DocumentNode node, String segment) {
        String nodeTypePattern = segment;
        String filterKey = null;
        String filterValue = null;

        int filterStart = segment.indexOf('[');
        if (filterStart != -1) {
            nodeTypePattern = segment.substring(0, filterStart);
            int filterEnd = segment.indexOf(']', filterStart);
            if (filterEnd == -1) {
                throw new IllegalArgumentException("Unclosed attribute selector in query segment: " + segment);
            }
            String filter = segment.substring(filterStart + 1, filterEnd).trim();
            if (filter.startsWith("@")) {
                filter = filter.substring(1);
                int eqIdx = filter.indexOf('=');
                if (eqIdx != -1) {
                    filterKey = filter.substring(0, eqIdx).trim();
                    String val = filter.substring(eqIdx + 1).trim();
                    if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    filterValue = val;
                } else {
                    filterKey = filter;
                }
            } else {
                throw new IllegalArgumentException("Attribute filter must start with '@' inside selector");
            }
        }

        List<DocumentNode> match = new ArrayList<>();
        if (matchesType(node, nodeTypePattern)) {
            if (matchesFilter(node, filterKey, filterValue)) {
                match.add(node);
            }
        }
        return match;
    }

    private List<DocumentNode> evaluateSegment(List<DocumentNode> candidates, String segment) {
        List<DocumentNode> nextCandidates = new ArrayList<>();
        
        String nodeTypePattern = segment;
        String filterKey = null;
        String filterValue = null;

        int filterStart = segment.indexOf('[');
        if (filterStart != -1) {
            nodeTypePattern = segment.substring(0, filterStart);
            int filterEnd = segment.indexOf(']', filterStart);
            if (filterEnd == -1) {
                throw new IllegalArgumentException("Unclosed attribute selector in query segment: " + segment);
            }
            String filter = segment.substring(filterStart + 1, filterEnd).trim();
            if (filter.startsWith("@")) {
                filter = filter.substring(1);
                int eqIdx = filter.indexOf('=');
                if (eqIdx != -1) {
                    filterKey = filter.substring(0, eqIdx).trim();
                    String val = filter.substring(eqIdx + 1).trim();
                    if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    filterValue = val;
                } else {
                    filterKey = filter;
                }
            } else {
                throw new IllegalArgumentException("Attribute filter must start with '@' inside selector");
            }
        }

        for (DocumentNode parent : candidates) {
            for (DocumentNode child : parent.getChildren()) {
                if (matchesType(child, nodeTypePattern)) {
                    if (matchesFilter(child, filterKey, filterValue)) {
                        nextCandidates.add(child);
                    }
                }
            }
        }

        return nextCandidates;
    }

    private boolean matchesType(DocumentNode node, String pattern) {
        if ("*".equals(pattern)) {
            return true;
        }
        return node.getNodeType().equalsIgnoreCase(pattern) || node.getId().equalsIgnoreCase(pattern);
    }

    private boolean matchesFilter(DocumentNode node, String filterKey, String filterValue) {
        if (filterKey == null) {
            return true;
        }
        String nodeAttrVal = node.getAttribute(filterKey);
        if ("id".equalsIgnoreCase(filterKey)) {
            nodeAttrVal = node.getId();
        }

        if (nodeAttrVal == null) {
            return false;
        }
        if (filterValue == null) {
            return true;
        }
        return nodeAttrVal.equals(filterValue);
    }
}

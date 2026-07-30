package com.aura.model.query;

import com.aura.model.DocumentNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Node representing a single step selector inside the AuraQuery AST.
 */
public class SelectorNode {
    private final String nodeTypePattern;
    private final boolean deep;
    private final List<Filter> filters = new ArrayList<>();

    public interface Filter {
        boolean match(DocumentNode node);
    }

    public static class AttributeFilter implements Filter {
        private final String key;
        private final String val;
        private final boolean equals;

        public AttributeFilter(String key, String val, boolean equals) {
            this.key = key;
            this.val = val;
            this.equals = equals;
        }

        @Override
        public boolean match(DocumentNode node) {
            String attrVal = "id".equalsIgnoreCase(key) ? node.getId() : node.getAttribute(key);
            if (attrVal == null) {
                return false;
            }
            if (val == null) {
                return true; // Existence check
            }
            return equals ? attrVal.equals(val) : !attrVal.equals(val);
        }
    }

    public static class IndexFilter implements Filter {
        private final int index;

        public IndexFilter(int index) {
            this.index = index;
        }

        @Override
        public boolean match(DocumentNode node) {
            // Evaluated by the parent engine context during list selections.
            return true;
        }

        public int getIndex() {
            return index;
        }
    }

    public SelectorNode(String nodeTypePattern, boolean deep) {
        this.nodeTypePattern = nodeTypePattern;
        this.deep = deep;
    }

    public String getNodeTypePattern() {
        return nodeTypePattern;
    }

    public boolean isDeep() {
        return deep;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public boolean matchesNode(DocumentNode node) {
        if (!"*".equals(nodeTypePattern) && !node.getNodeType().equalsIgnoreCase(nodeTypePattern) && !node.getId().equalsIgnoreCase(nodeTypePattern)) {
            return false;
        }
        for (Filter filter : filters) {
            if (!(filter instanceof IndexFilter)) {
                if (!filter.match(node)) {
                    return false;
                }
            }
        }
        return true;
    }
}

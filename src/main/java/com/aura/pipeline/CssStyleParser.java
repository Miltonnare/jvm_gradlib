package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.query.QueryExpression;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses simplified CSS stylesheets and applies properties to matching DocumentNodes.
 */
public class CssStyleParser {
    private final List<Rule> rules = new ArrayList<>();

    public static class Rule {
        private final String selector;
        private final Map<String, String> declarations;

        public Rule(String selector, Map<String, String> declarations) {
            this.selector = selector;
            this.declarations = declarations;
        }

        public String getSelector() { return selector; }
        public Map<String, String> getDeclarations() { return declarations; }
    }

    public void parse(String css) {
        Preconditions.checkNotNull(css, "CSS content cannot be null");
        rules.clear();

        int idx = 0;
        int len = css.length();

        while (idx < len) {
            // Skip whitespaces and comments
            idx = skipWhitespaceAndComments(css, idx);
            if (idx >= len) break;

            int braceOpen = css.indexOf('{', idx);
            if (braceOpen == -1) {
                throw new IllegalArgumentException("Malformed CSS: missing open brace '{' for selector at " + idx);
            }

            String selector = css.substring(idx, braceOpen).trim();
            int braceClose = css.indexOf('}', braceOpen);
            if (braceClose == -1) {
                throw new IllegalArgumentException("Malformed CSS: missing close brace '}' for selector: " + selector);
            }

            String block = css.substring(braceOpen + 1, braceClose).trim();
            Map<String, String> declarations = parseDeclarations(block);
            rules.add(new Rule(selector, declarations));

            idx = braceClose + 1;
        }
    }

    public void apply(DocumentNode root) {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        for (Rule rule : rules) {
            // Translate CSS selectors into AuraQuery expressions
            String queryStr = translateSelector(rule.getSelector());
            try {
                QueryExpression query = new QueryExpression(queryStr);
                List<DocumentNode> matched = query.evaluate(root);
                for (DocumentNode node : matched) {
                    for (Map.Entry<String, String> decl : rule.getDeclarations().entrySet()) {
                        node.setAttribute(decl.getKey(), decl.getValue());
                    }
                }
            } catch (Exception e) {
                // Catch matching exceptions
            }
        }
    }

    private Map<String, String> parseDeclarations(String block) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] decls = block.split(";");
        for (String decl : decls) {
            String trimmed = decl.trim();
            if (trimmed.isEmpty()) continue;

            int colon = trimmed.indexOf(':');
            if (colon == -1) {
                throw new IllegalArgumentException("Malformed CSS declaration: missing colon ':' in: " + trimmed);
            }

            String key = trimmed.substring(0, colon).trim();
            String val = trimmed.substring(colon + 1).trim();
            map.put(key, val);
        }
        return map;
    }

    private String translateSelector(String selector) {
        // Simple translation:
        // "#id" -> "//*[@id='id']"
        // ".class" -> "//*[@class='class']"
        // "tag" -> "//tag"
        String sel = selector.trim();
        if (sel.startsWith("#")) {
            return "//*[@id='" + sel.substring(1) + "']";
        } else if (sel.startsWith(".")) {
            return "//*[@class='" + sel.substring(1) + "']";
        }
        return "//" + sel;
    }

    private int skipWhitespaceAndComments(String css, int start) {
        int i = start;
        int len = css.length();
        while (i < len) {
            char c = css.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '/' && i + 1 < len && css.charAt(i + 1) == '*') {
                int commentEnd = css.indexOf("*/", i + 2);
                if (commentEnd == -1) {
                    throw new IllegalArgumentException("Unclosed CSS comment starting at " + i);
                }
                i = commentEnd + 2;
                continue;
            }
            break;
        }
        return i;
    }
}

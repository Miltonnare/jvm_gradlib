package com.aura.model.query;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser that compiles QueryToken streams into a list of executable SelectorNode nodes.
 */
public class QueryParser {
    private final List<QueryToken> tokens;
    private int idx;
    private boolean absolute = false;

    public QueryParser(List<QueryToken> tokens) {
        this.tokens = Preconditions.checkNotNull(tokens, "Tokens cannot be null");
        this.idx = 0;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public List<SelectorNode> parse() {
        List<SelectorNode> selectors = new ArrayList<>();
        
        // Check if query starts absolute
        boolean firstDeep = false;
        if (peek().getType() == QueryToken.Type.DOUBLE_SLASH) {
            firstDeep = true;
            consume();
        } else if (peek().getType() == QueryToken.Type.SLASH) {
            absolute = true;
            consume();
        }

        while (peek().getType() != QueryToken.Type.EOF) {
            selectors.add(parseSelector(selectors.isEmpty() ? firstDeep : false));

            if (peek().getType() == QueryToken.Type.SLASH) {
                consume();
                if (peek().getType() == QueryToken.Type.EOF) {
                    throw new IllegalArgumentException("Trailing slash in path query is prohibited");
                }
            } else if (peek().getType() == QueryToken.Type.DOUBLE_SLASH) {
                consume();
                if (peek().getType() == QueryToken.Type.EOF) {
                    throw new IllegalArgumentException("Trailing double-slash in path query is prohibited");
                }
                // Mark the NEXT selector as deep
                selectors.add(parseSelector(true));
            } else if (peek().getType() != QueryToken.Type.EOF) {
                throw new IllegalArgumentException("Path segments must be separated by slash '/' or '//' at position " + peek().getPosition());
            }
        }

        return selectors;
    }

    private SelectorNode parseSelector(boolean forceDeep) {
        boolean deep = forceDeep;
        String pattern = "*";

        if (peek().getType() == QueryToken.Type.STAR) {
            consume();
        } else if (peek().getType() == QueryToken.Type.IDENTIFIER) {
            pattern = peek().getValue();
            consume();
        } else {
            throw new IllegalArgumentException(String.format("Expected path identifier or '*' but got %s at position %d",
                peek().getType(), peek().getPosition()));
        }

        SelectorNode node = new SelectorNode(pattern, deep);

        // Parse optional filters: [@key = 'value'] or [index]
        while (peek().getType() == QueryToken.Type.LBRACKET) {
            consume(); // '['
            parseFilter(node);
            consume(QueryToken.Type.RBRACKET, "Expected closing bracket ']' in filter");
        }

        return node;
    }

    private void parseFilter(SelectorNode node) {
        if (peek().getType() == QueryToken.Type.NUMBER) {
            int index = Integer.parseInt(peek().getValue());
            consume();
            node.addFilter(new SelectorNode.IndexFilter(index));
            return;
        }

        if (peek().getType() == QueryToken.Type.AT) {
            consume(); // '@'
            
            consume(QueryToken.Type.IDENTIFIER, "Expected attribute key identifier after '@'");
            String key = previous().getValue();

            if (peek().getType() == QueryToken.Type.EQUALS) {
                consume();
                consume(QueryToken.Type.STRING, "Expected string value in attribute selector comparison");
                String val = previous().getValue();
                node.addFilter(new SelectorNode.AttributeFilter(key, val, true));
            } else if (peek().getType() == QueryToken.Type.NOT_EQUALS) {
                consume();
                consume(QueryToken.Type.STRING, "Expected string value in attribute selector comparison");
                String val = previous().getValue();
                node.addFilter(new SelectorNode.AttributeFilter(key, val, false));
            } else {
                // Key existence check only
                node.addFilter(new SelectorNode.AttributeFilter(key, null, true));
            }
            return;
        }

        throw new IllegalArgumentException("Invalid selector filter expression at position " + peek().getPosition());
    }

    private QueryToken peek() {
        if (idx >= tokens.size()) {
            return new QueryToken(QueryToken.Type.EOF, "", 0);
        }
        return tokens.get(idx);
    }

    private QueryToken previous() {
        Preconditions.checkState(idx > 0, "No previous token");
        return tokens.get(idx - 1);
    }

    private void consume() {
        if (idx < tokens.size()) {
            idx++;
        }
    }

    private void consume(QueryToken.Type expected, String errorMsg) {
        QueryToken curr = peek();
        if (curr.getType() != expected) {
            throw new IllegalArgumentException(errorMsg + ". Got " + curr.getType() + " at position " + curr.getPosition());
        }
        consume();
    }
}

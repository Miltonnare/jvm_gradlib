package com.aura.engine.template;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses AuraTemplate tokens and compiles them into a structured AST.
 */
public class TemplateParser {
    private final List<TemplateToken> tokens;
    private int idx = 0;

    public TemplateParser(List<TemplateToken> tokens) {
        this.tokens = Preconditions.checkNotNull(tokens, "Tokens cannot be null");
    }

    public TemplateAST.RootNode parse() {
        TemplateAST.RootNode root = new TemplateAST.RootNode();
        while (peek().getType() != TemplateToken.Type.EOF) {
            TemplateAST.Node node = parseNode();
            if (node != null) {
                root.addNode(node);
            }
        }
        return root;
    }

    private TemplateAST.Node parseNode() {
        TemplateToken curr = peek();
        if (curr.getType() == TemplateToken.Type.TEXT) {
            consume();
            return new TemplateAST.TextNode(curr.getValue());
        }

        if (curr.getType() == TemplateToken.Type.VAR_START) {
            consume(); // '{{'
            TemplateAST.Node node = parseBlockOrVariable();
            consume(TemplateToken.Type.VAR_END, "Expected closing tag '}}'");
            return node;
        }

        throw new IllegalArgumentException("Unexpected token in template: " + curr);
    }

    private TemplateAST.Node parseBlockOrVariable() {
        TemplateToken curr = peek();

        if (curr.getType() == TemplateToken.Type.IF) {
            consume(); // 'if'
            List<String> path = parsePath();
            TemplateAST.IfNode ifNode = new TemplateAST.IfNode(path);

            consume(TemplateToken.Type.VAR_END, "Expected closing tag '}}' after if condition");

            // Parse body until else or endif
            while (peek().getType() != TemplateToken.Type.EOF) {
                if (isBlockEnd(TemplateToken.Type.ELSE, TemplateToken.Type.ENDIF)) {
                    break;
                }
                TemplateAST.Node child = parseNode();
                if (child != null) {
                    ifNode.addToBody(child);
                }
            }

            if (peek().getType() == TemplateToken.Type.EOF) {
                throw new IllegalArgumentException("Unclosed if block in template");
            }

            // Check if else
            TemplateToken nextTag = peek(1);
            if (nextTag.getType() == TemplateToken.Type.ELSE) {
                consume(); // consume '{{'
                consume(); // consume 'else'
                consume(TemplateToken.Type.VAR_END, "Expected closing tag '}}' after else");

                while (peek().getType() != TemplateToken.Type.EOF) {
                    if (isBlockEnd(TemplateToken.Type.ENDIF)) {
                        break;
                    }
                    TemplateAST.Node child = parseNode();
                    if (child != null) {
                        ifNode.addToElseBody(child);
                    }
                }
            }

            // Consume endif tag
            consume(); // consume '{{'
            consume(TemplateToken.Type.ENDIF, "Expected 'endif'");

            return ifNode;
        }

        if (curr.getType() == TemplateToken.Type.FOR) {
            consume(); // 'for'
            consume(TemplateToken.Type.IDENTIFIER, "Expected loop variable identifier in for block");
            String loopVar = previous().getValue();

            consume(TemplateToken.Type.IN, "Expected keyword 'in' in for block");
            List<String> listPath = parsePath();
            TemplateAST.ForNode forNode = new TemplateAST.ForNode(loopVar, listPath);

            consume(TemplateToken.Type.VAR_END, "Expected closing tag '}}' after for condition");

            while (peek().getType() != TemplateToken.Type.EOF) {
                if (isBlockEnd(TemplateToken.Type.ENDFOR)) {
                    break;
                }
                TemplateAST.Node child = parseNode();
                if (child != null) {
                    forNode.addToBody(child);
                }
            }

            // Consume endfor tag
            consume(); // consume '{{'
            consume(TemplateToken.Type.ENDFOR, "Expected 'endfor'");

            return forNode;
        }

        // Default: parse variable with optional filters
        List<String> path = parsePath();
        TemplateAST.VariableNode varNode = new TemplateAST.VariableNode(path);

        while (peek().getType() == TemplateToken.Type.PIPE) {
            consume(); // '|'
            consume(TemplateToken.Type.IDENTIFIER, "Expected filter name identifier after pipe '|'");
            String filterName = previous().getValue();
            varNode.addFilter(resolveFilter(filterName));
        }

        return varNode;
    }

    private boolean isBlockEnd(TemplateToken.Type... endTypes) {
        if (peek().getType() == TemplateToken.Type.VAR_START) {
            TemplateToken tagToken = peek(1);
            for (TemplateToken.Type t : endTypes) {
                if (tagToken.getType() == t) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> parsePath() {
        List<String> path = new ArrayList<>();
        consume(TemplateToken.Type.IDENTIFIER, "Expected variable identifier path segment");
        path.add(previous().getValue());

        while (peek().getType() == TemplateToken.Type.DOT) {
            consume(); // '.'
            consume(TemplateToken.Type.IDENTIFIER, "Expected property identifier path segment after '.'");
            path.add(previous().getValue());
        }

        return path;
    }

    private TemplateAST.VariableNode.Filter resolveFilter(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "upper" -> input -> input != null ? input.toString().toUpperCase(Locale.ROOT) : "";
            case "lower" -> input -> input != null ? input.toString().toLowerCase(Locale.ROOT) : "";
            case "trim" -> input -> input != null ? input.toString().trim() : "";
            default -> throw new IllegalArgumentException("Unknown template filter: " + name);
        };
    }

    private TemplateToken peek() {
        if (idx >= tokens.size()) {
            return new TemplateToken(TemplateToken.Type.EOF, "", 0);
        }
        return tokens.get(idx);
    }

    private TemplateToken peek(int offset) {
        if (idx + offset >= tokens.size()) {
            return new TemplateToken(TemplateToken.Type.EOF, "", 0);
        }
        return tokens.get(idx + offset);
    }

    private TemplateToken previous() {
        Preconditions.checkState(idx > 0, "No previous token");
        return tokens.get(idx - 1);
    }

    private void consume() {
        if (idx < tokens.size()) {
            idx++;
        }
    }

    private void consume(TemplateToken.Type expected, String errorMsg) {
        TemplateToken curr = peek();
        if (curr.getType() != expected) {
            throw new IllegalArgumentException(errorMsg + ". Got " + curr.getType() + " at position " + curr.getPosition());
        }
        consume();
    }
}

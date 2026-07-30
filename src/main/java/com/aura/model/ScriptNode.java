package com.aura.model;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node representing an executable expression script within the document structure.
 */
public class ScriptNode extends DocumentNode {
    private final String expression;

    public ScriptNode(String id, String expression) {
        super(id);
        this.expression = Preconditions.checkNotNull(expression, "Expression cannot be null");
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String getNodeType() {
        return "script";
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Simple recursive expression evaluator.
     * Supports basic arithmetic (+, -, *, /) and variable substitution.
     */
    public double evaluate(Map<String, Double> context) {
        List<String> tokens = tokenize(expression);
        if (tokens.isEmpty()) {
            return 0.0;
        }
        return new Evaluator(tokens, context).parseAndEval();
    }

    private List<String> tokenize(String expr) {
        List<String> list = new ArrayList<>();
        int i = 0;
        int len = expr.length();
        while (i < len) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
                list.add(String.valueOf(c));
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i));
                    i++;
                }
                list.add(sb.toString());
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < len && (Character.isLetterOrDigit(expr.charAt(i)) || expr.charAt(i) == '_')) {
                    sb.append(expr.charAt(i));
                    i++;
                }
                list.add(sb.toString());
                continue;
            }
            // Ignore other symbols or throw error
            list.add(String.valueOf(c));
            i++;
        }
        return list;
    }

    private static class Evaluator {
        private final List<String> tokens;
        private final Map<String, Double> context;
        private int idx = 0;

        Evaluator(List<String> tokens, Map<String, Double> context) {
            this.tokens = tokens;
            this.context = context;
        }

        double parseAndEval() {
            double result = parseTerm();
            while (true) {
                String next = peek();
                if ("+".equals(next)) {
                    consume();
                    result += parseTerm();
                } else if ("-".equals(next)) {
                    consume();
                    result -= parseTerm();
                } else {
                    break;
                }
            }
            return result;
        }

        private double parseTerm() {
            double result = parseFactor();
            while (true) {
                String next = peek();
                if ("*".equals(next)) {
                    consume();
                    result *= parseFactor();
                } else if ("/".equals(next)) {
                    consume();
                    double divisor = parseFactor();
                    if (divisor == 0.0) {
                        throw new ArithmeticException("Division by zero in expression evaluation");
                    }
                    result /= divisor;
                } else {
                    break;
                }
            }
            return result;
        }

        private double parseFactor() {
            String next = peek();
            if ("(".equals(next)) {
                consume(); // '('
                double result = parseAndEval();
                consume(")"); // ')'
                return result;
            }
            if ("-".equals(next)) {
                consume();
                return -parseFactor();
            }
            if ("+".equals(next)) {
                consume();
                return parseFactor();
            }

            consume();
            if (next == null) {
                throw new IllegalArgumentException("Unexpected end of expression");
            }

            // Check if it's a number
            try {
                return Double.parseDouble(next);
            } catch (NumberFormatException e) {
                // Treat as variable
                if (context != null && context.containsKey(next)) {
                    return context.get(next);
                }
                throw new IllegalArgumentException("Undefined variable or invalid number: " + next);
            }
        }

        private String peek() {
            if (idx >= tokens.size()) return null;
            return tokens.get(idx);
        }

        private void consume() {
            if (idx < tokens.size()) {
                idx++;
            }
        }

        private void consume(String expected) {
            String val = peek();
            if (!expected.equals(val)) {
                throw new IllegalArgumentException(String.format("Expected token '%s' but got '%s' at token position %d", expected, val, idx));
            }
            consume();
        }
    }
}

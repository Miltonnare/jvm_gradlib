package com.aura.engine.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compiled AST nodes for the AuraTemplate engine.
 */
public final class TemplateAST {

    private TemplateAST() {
        // Prevent instantiation
    }

    public static abstract class Node {
        public abstract void render(TemplateContext ctx, StringBuilder sb);
    }

    public static class TextNode extends Node {
        private final String text;

        public TextNode(String text) {
            this.text = text;
        }

        @Override
        public void render(TemplateContext ctx, StringBuilder sb) {
            sb.append(text);
        }
    }

    public static class VariableNode extends Node {
        private final List<String> path;
        private final List<Filter> filters = new ArrayList<>();

        public interface Filter {
            Object apply(Object input);
        }

        public VariableNode(List<String> path) {
            this.path = path;
        }

        public void addFilter(Filter filter) {
            filters.add(filter);
        }

        @Override
        public void render(TemplateContext ctx, StringBuilder sb) {
            Object val = ctx.resolve(path);
            if (val != null) {
                for (Filter filter : filters) {
                    val = filter.apply(val);
                }
                sb.append(val);
            }
        }
    }

    public static class IfNode extends Node {
        private final List<String> conditionPath;
        private final List<Node> body = new ArrayList<>();
        private final List<Node> elseBody = new ArrayList<>();

        public IfNode(List<String> conditionPath) {
            this.conditionPath = conditionPath;
        }

        public void addToBody(Node node) {
            body.add(node);
        }

        public void addToElseBody(Node node) {
            elseBody.add(node);
        }

        @Override
        public void render(TemplateContext ctx, StringBuilder sb) {
            Object cond = ctx.resolve(conditionPath);
            boolean truthy = isTruthy(cond);

            List<Node> target = truthy ? body : elseBody;
            for (Node n : target) {
                n.render(ctx, sb);
            }
        }

        private boolean isTruthy(Object cond) {
            if (cond == null) return false;
            if (cond instanceof Boolean b) return b;
            if (cond instanceof Number n) return n.doubleValue() != 0.0;
            if (cond instanceof String s) return !s.isEmpty();
            if (cond instanceof java.util.Collection<?> c) return !c.isEmpty();
            if (cond instanceof Map<?, ?> m) return !m.isEmpty();
            return true;
        }
    }

    public static class ForNode extends Node {
        private final String loopVar;
        private final List<String> listPath;
        private final List<Node> body = new ArrayList<>();

        public ForNode(String loopVar, List<String> listPath) {
            this.loopVar = loopVar;
            this.listPath = listPath;
        }

        public void addToBody(Node node) {
            body.add(node);
        }

        @Override
        public void render(TemplateContext ctx, StringBuilder sb) {
            Object listObj = ctx.resolve(listPath);
            if (listObj instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    ctx.pushFrame();
                    ctx.setLocal(loopVar, item);
                    for (Node n : body) {
                        n.render(ctx, sb);
                    }
                    ctx.popFrame();
                }
            }
        }
    }

    public static class RootNode extends Node {
        private final List<Node> nodes = new ArrayList<>();

        public void addNode(Node node) {
            nodes.add(node);
        }

        public List<Node> getNodes() {
            return nodes;
        }

        @Override
        public void render(TemplateContext ctx, StringBuilder sb) {
            for (Node n : nodes) {
                n.render(ctx, sb);
            }
        }
    }
}

package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

/**
 * Custom parser to reconstruct a DocumentNode layout tree from HTML5 strings.
 */
public class HtmlImporter implements TransformationPipeline.Stage<String, DocumentNode> {
    private static final int MAX_NESTING = 32;

    public DocumentNode importHtml(String html) {
        Preconditions.checkNotNull(html, "HTML string cannot be null");
        List<Token> tokens = tokenize(html);
        return parse(tokens);
    }

    private static class Token {
        enum Type { OPEN_TAG, CLOSE_TAG, SELF_CLOSE_TAG, TEXT }
        final Type type;
        final String tagName;
        final Map<String, String> attributes;
        final String text;

        Token(Type type, String tagName, Map<String, String> attributes, String text) {
            this.type = type;
            this.tagName = tagName;
            this.attributes = attributes;
            this.text = text;
        }
    }

    private List<Token> tokenize(String html) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = html.length();

        while (i < len) {
            char c = html.charAt(i);
            if (c == '<') {
                int closeIdx = html.indexOf('>', i);
                if (closeIdx == -1) {
                    throw new IllegalArgumentException("Malformed HTML: unclosed tag starting at " + i);
                }
                String tagContent = html.substring(i + 1, closeIdx).trim();
                i = closeIdx + 1;

                if (tagContent.startsWith("/")) {
                    // Close tag
                    String name = tagContent.substring(1).trim();
                    tokens.add(new Token(Token.Type.CLOSE_TAG, name, null, null));
                } else if (tagContent.endsWith("/")) {
                    // Self-closing tag
                    String tagBody = tagContent.substring(0, tagContent.length() - 1).trim();
                    String name = getTagName(tagBody);
                    Map<String, String> attrs = parseAttributes(tagBody);
                    tokens.add(new Token(Token.Type.SELF_CLOSE_TAG, name, attrs, null));
                } else {
                    // Open tag
                    String name = getTagName(tagContent);
                    Map<String, String> attrs = parseAttributes(tagContent);
                    // Special check: img and br are self-closing by standard html
                    if ("img".equalsIgnoreCase(name) || "br".equalsIgnoreCase(name)) {
                        tokens.add(new Token(Token.Type.SELF_CLOSE_TAG, name, attrs, null));
                    } else {
                        tokens.add(new Token(Token.Type.OPEN_TAG, name, attrs, null));
                    }
                }
            } else {
                int nextTag = html.indexOf('<', i);
                String text;
                if (nextTag == -1) {
                    text = html.substring(i);
                    i = len;
                } else {
                    text = html.substring(i, nextTag);
                    i = nextTag;
                }
                if (!text.trim().isEmpty()) {
                    tokens.add(new Token(Token.Type.TEXT, null, null, text.trim()));
                }
            }
        }
        return tokens;
    }

    private String getTagName(String tagContent) {
        int space = tagContent.indexOf(' ');
        return space == -1 ? tagContent : tagContent.substring(0, space);
    }

    private Map<String, String> parseAttributes(String tagContent) {
        Map<String, String> attrs = new HashMap<>();
        int space = tagContent.indexOf(' ');
        if (space == -1) {
            return attrs;
        }
        String body = tagContent.substring(space + 1).trim();
        int idx = 0;
        int len = body.length();

        while (idx < len) {
            while (idx < len && Character.isWhitespace(body.charAt(idx))) {
                idx++;
            }
            if (idx >= len) break;

            int eq = body.indexOf('=', idx);
            if (eq == -1) {
                // Key existence check
                String key = body.substring(idx).trim();
                if (!key.isEmpty()) {
                    attrs.put(key, "true");
                }
                break;
            }
            String key = body.substring(idx, eq).trim();
            idx = eq + 1;

            while (idx < len && Character.isWhitespace(body.charAt(idx))) {
                idx++;
            }
            if (idx >= len) break;

            char quote = body.charAt(idx);
            if (quote == '"' || quote == '\'') {
                int endQuote = body.indexOf(quote, idx + 1);
                if (endQuote == -1) {
                    // String fallback
                    String val = body.substring(idx + 1).trim();
                    attrs.put(key, val);
                    break;
                }
                String val = body.substring(idx + 1, endQuote);
                attrs.put(key, val);
                idx = endQuote + 1;
            } else {
                int nextSpace = body.indexOf(' ', idx);
                String val;
                if (nextSpace == -1) {
                    val = body.substring(idx).trim();
                    idx = len;
                } else {
                    val = body.substring(idx, nextSpace);
                    idx = nextSpace + 1;
                }
                attrs.put(key, val);
            }
        }
        return attrs;
    }

    private DocumentNode parse(List<Token> tokens) {
        Stack<DocumentNode> stack = new Stack<>();
        DocumentNode root = null;
        
        // Mock default container row/col tracker for table cells
        Stack<TableLayoutState> tableStack = new Stack<>();

        for (Token t : tokens) {
            if (stack.size() > MAX_NESTING) {
                throw new IllegalStateException("HTML nesting exceeds maximum nesting depth");
            }

            switch (t.type) {
                case OPEN_TAG -> {
                    DocumentNode node = createNode(t.tagName, t.attributes);
                    if (root == null) {
                        root = node;
                    } else if (!stack.isEmpty()) {
                        DocumentNode parent = stack.peek();
                        if (parent instanceof TableNode tbl && "tr".equalsIgnoreCase(node.getNodeType())) {
                            // TableNode handles cells, tr is just logical, but let's track state
                            tableStack.peek().currentRow++;
                            tableStack.peek().currentCol = -1;
                        } else if ("td".equalsIgnoreCase(node.getNodeType()) || "th".equalsIgnoreCase(node.getNodeType())) {
                            // Table cell
                            tableStack.peek().currentCol++;
                        } else {
                            parent.addChild(node);
                        }
                    }
                    
                    if ("table".equalsIgnoreCase(t.tagName)) {
                        int r = t.attributes.containsKey("rows") ? Integer.parseInt(t.attributes.get("rows")) : 8;
                        int c = t.attributes.containsKey("cols") ? Integer.parseInt(t.attributes.get("cols")) : 8;
                        tableStack.push(new TableLayoutState(r, c));
                    }
                    
                    stack.push(node);
                }
                case CLOSE_TAG -> {
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Orphan close tag: </" + t.tagName + ">");
                    }
                    DocumentNode closed = stack.pop();
                    if (!matchesTagName(closed, t.tagName)) {
                        throw new IllegalArgumentException(String.format("Mismatched HTML close tag: expected </%s> but got </%s>",
                            closed.getNodeType(), t.tagName));
                    }
                    if ("table".equalsIgnoreCase(t.tagName)) {
                        tableStack.pop();
                    }
                }
                case SELF_CLOSE_TAG -> {
                    DocumentNode node = createNode(t.tagName, t.attributes);
                    if (root == null) {
                        root = node;
                    } else if (!stack.isEmpty()) {
                        DocumentNode parent = stack.peek();
                        parent.addChild(node);
                    }
                }
                case TEXT -> {
                    if (!stack.isEmpty()) {
                        DocumentNode parent = stack.peek();
                        if (parent instanceof ParagraphNode p) {
                            p.setText(p.getText() + (p.getText().isEmpty() ? "" : " ") + t.text);
                        } else {
                            // Create inline paragraph under parent
                            ParagraphNode p = new ParagraphNode(UUID.randomUUID().toString(), t.text);
                            
                            // If parent is a TableRow simulation (represented by cell tags)
                            if (!tableStack.isEmpty() && ("td".equalsIgnoreCase(parent.getNodeType()) || "th".equalsIgnoreCase(parent.getNodeType()))) {
                                TableLayoutState state = tableStack.peek();
                                // Find table node (must be further down the stack)
                                TableNode tblNode = findTableNode(stack);
                                if (tblNode != null) {
                                    tblNode.setCell(state.currentRow, state.currentCol, p);
                                }
                            } else {
                                parent.addChild(p);
                            }
                        }
                    }
                }
            }
        }

        if (root == null) {
            throw new IllegalArgumentException("Empty HTML document");
        }
        return root;
    }

    private TableNode findTableNode(Stack<DocumentNode> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i) instanceof TableNode tbl) {
                return tbl;
            }
        }
        return null;
    }

    private DocumentNode createNode(String name, Map<String, String> attrs) {
        String id = attrs.getOrDefault("id", UUID.randomUUID().toString());
        DocumentNode node;
        switch (name.toLowerCase()) {
            case "p" -> node = new ParagraphNode(id, "");
            case "img" -> {
                String src = attrs.getOrDefault("src", "");
                int w = attrs.containsKey("width") ? Integer.parseInt(attrs.get("width")) : 100;
                int h = attrs.containsKey("height") ? Integer.parseInt(attrs.get("height")) : 100;
                node = new ImageNode(id, src, w, h);
            }
            case "table" -> {
                int r = attrs.containsKey("rows") ? Integer.parseInt(attrs.get("rows")) : 8;
                int c = attrs.containsKey("cols") ? Integer.parseInt(attrs.get("cols")) : 8;
                node = new TableNode(id, r, c);
            }
            case "code" -> {
                // Treats code tag as script expression
                node = new ScriptNode(id, "");
            }
            default -> {
                // Fallback generic tag container
                node = new DocumentNode(id) {
                    @Override public String getNodeType() { return name; }
                    @Override public void accept(DocumentVisitor visitor) {}
                };
            }
        }

        // Apply attributes
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            if (!"id".equalsIgnoreCase(attr.getKey())) {
                node.setAttribute(attr.getKey(), attr.getValue());
            }
        }
        return node;
    }

    private static class TableLayoutState {
        int rows;
        int cols;
        int currentRow = -1;
        int currentCol = -1;

        TableLayoutState(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }
    }

    private boolean matchesTagName(DocumentNode node, String tagName) {
        String nodeType = node.getNodeType();
        switch (tagName.toLowerCase()) {
            case "p" -> { return "paragraph".equalsIgnoreCase(nodeType); }
            case "img" -> { return "image".equalsIgnoreCase(nodeType); }
            case "code" -> { return "script".equalsIgnoreCase(nodeType); }
            default -> { return nodeType.equalsIgnoreCase(tagName); }
        }
    }

    @Override
    public DocumentNode process(String input) {
        return importHtml(input);
    }
}

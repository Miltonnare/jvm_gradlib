package com.aura.pipeline;

import com.aura.config.XonArray;
import com.aura.config.XonObject;
import com.aura.config.XonPrimitive;
import com.aura.config.XonValue;
import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import java.util.Map;

/**
 * Converts Document Graphs/Nodes to and from Xon Config structures, integrating 
 * the parsing and model subsystems.
 */
public final class FormatConverter {

    private FormatConverter() {
        // Prevent instantiation
    }

    /**
     * Converts a DocumentNode hierarchy into a XonObject structure.
     */
    public static XonObject toXon(DocumentNode node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        XonObject obj = new XonObject();
        obj.put("id", new XonPrimitive(node.getId()));
        obj.put("type", new XonPrimitive(node.getNodeType()));

        // Add class-specific attributes
        if (node instanceof ParagraphNode p) {
            obj.put("text", new XonPrimitive(p.getText()));
        } else if (node instanceof ImageNode img) {
            obj.put("src", new XonPrimitive(img.getSrc()));
            obj.put("width", new XonPrimitive(img.getWidth()));
            obj.put("height", new XonPrimitive(img.getHeight()));
        } else if (node instanceof TableNode tbl) {
            obj.put("rows", new XonPrimitive(tbl.getRowCount()));
            obj.put("cols", new XonPrimitive(tbl.getColCount()));
            XonArray cellsArr = new XonArray();
            for (int r = 0; r < tbl.getRowCount(); r++) {
                for (int c = 0; c < tbl.getColCount(); c++) {
                    DocumentNode cell = tbl.getCell(r, c);
                    if (cell != null) {
                        XonObject cellObj = toXon(cell);
                        cellObj.put("row", new XonPrimitive(r));
                        cellObj.put("col", new XonPrimitive(c));
                        cellsArr.add(cellObj);
                    }
                }
            }
            obj.put("cells", cellsArr);
        } else if (node instanceof ScriptNode s) {
            obj.put("expression", new XonPrimitive(s.getExpression()));
        }

        // Add common attributes
        if (!node.getAttributes().isEmpty()) {
            XonObject attrObj = new XonObject();
            for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
                attrObj.put(attr.getKey(), new XonPrimitive(attr.getValue()));
            }
            obj.put("attributes", attrObj);
        }

        // Add children
        if (!node.getChildren().isEmpty()) {
            XonArray childArr = new XonArray();
            for (DocumentNode child : node.getChildren()) {
                childArr.add(toXon(child));
            }
            obj.put("children", childArr);
        }

        return obj;
    }

    /**
     * Converts a XonObject back into a DocumentNode.
     */
    public static DocumentNode fromXon(XonObject obj) {
        Preconditions.checkNotNull(obj, "XonObject cannot be null");
        Preconditions.checkArgument(obj.has("id") && obj.get("id").isPrimitive(), "Missing node id");
        Preconditions.checkArgument(obj.has("type") && obj.get("type").isPrimitive(), "Missing node type");

        String id = obj.get("id").asString();
        String type = obj.get("type").asString();

        DocumentNode node;
        switch (type.toLowerCase()) {
            case "paragraph" -> {
                String text = obj.has("text") ? obj.get("text").asString() : "";
                node = new ParagraphNode(id, text);
            }
            case "image" -> {
                String src = obj.has("src") ? obj.get("src").asString() : "";
                int w = obj.has("width") ? obj.get("width").asInt() : 0;
                int h = obj.has("height") ? obj.get("height").asInt() : 0;
                node = new ImageNode(id, src, w, h);
            }
            case "table" -> {
                int r = obj.has("rows") ? obj.get("rows").asInt() : 1;
                int c = obj.has("cols") ? obj.get("cols").asInt() : 1;
                TableNode tbl = new TableNode(id, r, c);
                if (obj.has("cells") && obj.get("cells").isArray()) {
                    XonArray cellsArr = (XonArray) obj.get("cells");
                    for (XonValue cellVal : cellsArr.asArray()) {
                        if (cellVal.isObject()) {
                            XonObject cellObj = (XonObject) cellVal;
                            int cellRow = cellObj.has("row") ? cellObj.get("row").asInt() : 0;
                            int cellCol = cellObj.has("col") ? cellObj.get("col").asInt() : 0;
                            tbl.setCell(cellRow, cellCol, fromXon(cellObj));
                        }
                    }
                }
                node = tbl;
            }
            case "script" -> {
                String expr = obj.has("expression") ? obj.get("expression").asString() : "0";
                node = new ScriptNode(id, expr);
            }
            default -> {
                // General node fallback
                node = new DocumentNode(id) {
                    @Override public String getNodeType() { return type; }
                    @Override public void accept(DocumentVisitor visitor) {}
                };
            }
        }

        // Parse common attributes
        if (obj.has("attributes") && obj.get("attributes").isObject()) {
            XonObject attrObj = (XonObject) obj.get("attributes");
            for (Map.Entry<String, XonValue> entry : attrObj.asObject().entrySet()) {
                node.setAttribute(entry.getKey(), entry.getValue().asString());
            }
        }

        // Parse children
        if (obj.has("children") && obj.get("children").isArray()) {
            XonArray childArr = (XonArray) obj.get("children");
            for (XonValue childVal : childArr.asArray()) {
                if (childVal.isObject()) {
                    node.addChild(fromXon((XonObject) childVal));
                }
            }
        }

        return node;
    }
}

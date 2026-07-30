package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.List;
import java.util.Map;

/**
 * Serializes XonValue AST trees back into pretty-printed Xon Config Format string representation.
 */
public class XonWriter {
    private final int indentWidth;

    public XonWriter() {
        this(2);
    }

    public XonWriter(int indentWidth) {
        Preconditions.checkArgument(indentWidth >= 0, "Indent width must be non-negative");
        this.indentWidth = indentWidth;
    }

    public String write(XonValue root) {
        Preconditions.checkNotNull(root, "XonValue cannot be null");
        StringBuilder sb = new StringBuilder();
        writeValue(root, sb, 0);
        return sb.toString();
    }

    private void writeValue(XonValue value, StringBuilder sb, int indent) {
        if (value.isObject()) {
            writeObject((XonObject) value, sb, indent);
        } else if (value.isArray()) {
            writeArray((XonArray) value, sb, indent);
        } else if (value.isString()) {
            writeString(value.asString(), sb);
        } else if (value.isInt()) {
            sb.append(value.asInt());
        } else if (value.isDouble()) {
            sb.append(value.asDouble());
        } else if (value.isBoolean()) {
            sb.append(value.asBoolean());
        } else if (value.isNull()) {
            sb.append("null");
        }
    }

    private void writeObject(XonObject obj, StringBuilder sb, int indent) {
        sb.append("{\n");
        int nextIndent = indent + indentWidth;
        Map<String, XonValue> fields = obj.asObject();

        for (Map.Entry<String, XonValue> entry : fields.entrySet()) {
            appendIndent(sb, nextIndent);
            sb.append(entry.getKey()).append(": ");
            writeValue(entry.getValue(), sb, nextIndent);
            sb.append("\n");
        }

        appendIndent(sb, indent);
        sb.append("}");
    }

    private void writeArray(XonArray array, StringBuilder sb, int indent) {
        List<XonValue> list = array.asArray();
        if (list.isEmpty()) {
            sb.append("[]");
            return;
        }

        // Check if all elements are primitive to format inline
        boolean allPrimitive = true;
        for (XonValue item : list) {
            if (item.isObject() || item.isArray()) {
                allPrimitive = false;
                break;
            }
        }

        if (allPrimitive && list.size() <= 6) {
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                writeValue(list.get(i), sb, 0);
            }
            sb.append("]");
        } else {
            sb.append("[\n");
            int nextIndent = indent + indentWidth;
            for (int i = 0; i < list.size(); i++) {
                appendIndent(sb, nextIndent);
                writeValue(list.get(i), sb, nextIndent);
                if (i < list.size() - 1) sb.append(",");
                sb.append("\n");
            }
            appendIndent(sb, indent);
            sb.append("]");
        }
    }

    private void writeString(String str, StringBuilder sb) {
        sb.append("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append("\"");
    }

    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }
}

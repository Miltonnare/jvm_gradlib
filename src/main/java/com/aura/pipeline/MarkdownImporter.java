package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Custom parser to reconstruct a DocumentNode layout tree from Markdown strings.
 */
public class MarkdownImporter implements TransformationPipeline.Stage<String, DocumentNode> {

    public DocumentNode importMarkdown(String md) {
        Preconditions.checkNotNull(md, "Markdown string cannot be null");
        if (md.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty Markdown document");
        }
        String[] lines = md.split("\\r?\\n");
        
        // Root container document
        DocumentNode root = new DocumentNode("root") {
            @Override public String getNodeType() { return "document"; }
            @Override public void accept(DocumentVisitor visitor) {}
        };

        int idx = 0;
        int len = lines.length;
        while (idx < len) {
            String line = lines[idx].trim();
            if (line.isEmpty()) {
                idx++;
                continue;
            }

            // 1. Math block (ScriptNode)
            if (line.startsWith("```math")) {
                StringBuilder exprBuf = new StringBuilder();
                idx++;
                while (idx < len && !lines[idx].trim().equals("```")) {
                    exprBuf.append(lines[idx]).append("\n");
                    idx++;
                }
                if (idx >= len) {
                    throw new IllegalArgumentException("Malformed Markdown: unclosed math block code block");
                }
                idx++; // skip close ```
                ScriptNode script = new ScriptNode(UUID.randomUUID().toString(), exprBuf.toString().trim());
                root.addChild(script);
                continue;
            }

            // 2. Table Block
            if (line.startsWith("|")) {
                List<String[]> tableLines = new ArrayList<>();
                while (idx < len && lines[idx].trim().startsWith("|")) {
                    String row = lines[idx].trim();
                    // Split by '|' but skip leading/trailing empty cells
                    String[] cells = splitTableRow(row);
                    tableLines.add(cells);
                    idx++;
                }

                if (tableLines.size() < 2) {
                    throw new IllegalArgumentException("Malformed Markdown Table: must contain header and separator rows");
                }

                // First line is header, second line is separator (e.g. | --- |)
                String[] headerCells = tableLines.get(0);
                String[] sepCells = tableLines.get(1);
                // Check if separator is valid
                for (String sep : sepCells) {
                    if (!sep.matches("-+")) {
                        throw new IllegalArgumentException("Malformed Markdown Table: invalid separator row: " + String.join(" | ", sepCells));
                    }
                }

                int cols = headerCells.length;
                int rows = tableLines.size() - 1; // subtract separator row
                TableNode tblNode = new TableNode(UUID.randomUUID().toString(), rows, cols);

                // Populate Header
                for (int c = 0; c < cols; c++) {
                    tblNode.setCell(0, c, new ParagraphNode(UUID.randomUUID().toString(), headerCells[c]));
                }

                // Populate Data Rows
                for (int r = 2; r < tableLines.size(); r++) {
                    String[] rowCells = tableLines.get(r);
                    for (int c = 0; c < cols && c < rowCells.length; c++) {
                        if (!rowCells[c].isEmpty()) {
                            tblNode.setCell(r - 1, c, new ParagraphNode(UUID.randomUUID().toString(), rowCells[c]));
                        }
                    }
                }

                root.addChild(tblNode);
                continue;
            }

            // 3. Image block: ![alt](src)
            if (line.startsWith("![")) {
                int altClose = line.indexOf("](");
                if (altClose != -1 && line.endsWith(")")) {
                    String alt = line.substring(2, altClose).trim();
                    String src = line.substring(altClose + 2, line.length() - 1).trim();
                    ImageNode img = new ImageNode(UUID.randomUUID().toString(), src, 200, 200);
                    img.setAttribute("alt", alt);
                    root.addChild(img);
                    idx++;
                    continue;
                }
            }

            // 4. Default Paragraph block
            StringBuilder paraBuf = new StringBuilder(line);
            idx++;
            while (idx < len && !lines[idx].trim().isEmpty() && !lines[idx].trim().startsWith("|") && !lines[idx].trim().startsWith("```") && !lines[idx].trim().startsWith("![")) {
                paraBuf.append(" ").append(lines[idx].trim());
                idx++;
            }
            ParagraphNode para = new ParagraphNode(UUID.randomUUID().toString(), paraBuf.toString());
            root.addChild(para);
        }

        return root;
    }

    private String[] splitTableRow(String row) {
        String cleaned = row.trim();
        if (cleaned.startsWith("|")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("|")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        String[] raw = cleaned.split("\\|");
        String[] cells = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            cells[i] = raw[i].trim();
        }
        return cells;
    }

    @Override
    public DocumentNode process(String input) {
        return importMarkdown(input);
    }
}

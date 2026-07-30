package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traverses document graphs to compute statistics on characters, word count,
 * average word length, element distribution, and nesting dimensions.
 */
public class DocumentStatistics implements DocumentNode.DocumentVisitor, TransformationPipeline.Stage<DocumentNode, Map<String, Object>> {
    private int paragraphCount;
    private int imageCount;
    private int tableCount;
    private int cellCount;
    private int scriptCount;
    private long characterCount;
    private long wordCount;

    public Map<String, Object> analyze(DocumentNode root) {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        paragraphCount = 0;
        imageCount = 0;
        tableCount = 0;
        cellCount = 0;
        scriptCount = 0;
        characterCount = 0;
        wordCount = 0;

        traverse(root);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("paragraphCount", paragraphCount);
        stats.put("imageCount", imageCount);
        stats.put("tableCount", tableCount);
        stats.put("cellCount", cellCount);
        stats.put("scriptCount", scriptCount);
        stats.put("characterCount", characterCount);
        stats.put("wordCount", wordCount);
        stats.put("avgWordLength", wordCount == 0 ? 0.0 : (double) characterCount / wordCount);

        return stats;
    }

    private void traverse(DocumentNode node) {
        node.accept(this);
        if (!(node instanceof TableNode)) {
            for (DocumentNode child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        paragraphCount++;
        String text = paragraph.getText();
        if (text != null && !text.isEmpty()) {
            characterCount += text.length();
            String[] words = text.split("\\s+");
            for (String w : words) {
                if (!w.trim().isEmpty()) {
                    wordCount++;
                }
            }
        }
    }

    @Override
    public void visit(ImageNode image) {
        imageCount++;
    }

    @Override
    public void visit(TableNode table) {
        tableCount++;
        int rows = table.getRowCount();
        int cols = table.getColCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                DocumentNode cell = table.getCell(r, c);
                if (cell != null) {
                    cellCount++;
                    traverse(cell);
                }
            }
        }
    }

    @Override
    public void visit(ScriptNode script) {
        scriptCount++;
    }

    @Override
    public Map<String, Object> process(DocumentNode input) {
        return analyze(input);
    }
}

package com.aura.model;

import com.aura.internal.Preconditions;

/**
 * Node representing structured tabular rows and cells.
 */
public class TableNode extends DocumentNode {
    private final int rows;
    private final int cols;
    private final DocumentNode[][] cells;

    public TableNode(String id, int rows, int cols) {
        super(id);
        Preconditions.checkArgument(rows > 0, "Rows must be positive");
        Preconditions.checkArgument(cols > 0, "Columns must be positive");
        this.rows = rows;
        this.cols = cols;
        this.cells = new DocumentNode[rows][cols];
    }

    public void setCell(int r, int c, DocumentNode cellNode) {
        Preconditions.checkPositionIndexes(r, r, rows);
        Preconditions.checkPositionIndexes(c, c, cols);
        cells[r][c] = cellNode;
        if (cellNode != null) {
            addChild(cellNode);
        }
    }

    public DocumentNode getCell(int r, int c) {
        Preconditions.checkPositionIndexes(r, r, rows);
        Preconditions.checkPositionIndexes(c, c, cols);
        return cells[r][c];
    }

    public int getRowCount() { return rows; }
    public int getColCount() { return cols; }

    @Override
    public String getNodeType() {
        return "table";
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }
}

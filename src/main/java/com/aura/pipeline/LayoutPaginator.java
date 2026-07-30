package com.aura.pipeline;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.ImageNode;
import com.aura.model.ParagraphNode;
import com.aura.model.ScriptNode;
import com.aura.model.TableNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Advanced Layout engine that paginates a Document Node Graph 
 * onto discrete pages based on dimensions, wrapping paragraphs, and splitting tables.
 */
public class LayoutPaginator implements DocumentNode.DocumentVisitor, TransformationPipeline.Stage<DocumentNode, List<LayoutPaginator.Page>> {
    private final double pageWidth;
    private final double pageHeight;
    private final double marginTop;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;

    private final List<Page> pages = new ArrayList<>();
    private Page currentPage;
    private double currentY;

    public static final class Page {
        private final int pageNumber;
        private final List<PlacedElement> elements = new ArrayList<>();

        public Page(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageNumber() { return pageNumber; }
        public List<PlacedElement> getElements() { return Collections.unmodifiableList(elements); }
        
        public void addElement(PlacedElement el) {
            elements.add(el);
        }
    }

    public static final class PlacedElement {
        private final DocumentNode node;
        private final String renderText;
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        public PlacedElement(DocumentNode node, String renderText, double x, double y, double width, double height) {
            this.node = node;
            this.renderText = renderText;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public DocumentNode getNode() { return node; }
        public String getRenderText() { return renderText; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }

    public LayoutPaginator(double pageWidth, double pageHeight, double margin) {
        this(pageWidth, pageHeight, margin, margin, margin, margin);
    }

    public LayoutPaginator(double pageWidth, double pageHeight, double marginTop, double marginBottom, double marginLeft, double marginRight) {
        Preconditions.checkArgument(pageWidth > 0, "Page width must be positive");
        Preconditions.checkArgument(pageHeight > 0, "Page height must be positive");
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    public List<Page> paginate(DocumentNode root) {
        Preconditions.checkNotNull(root, "Root node cannot be null");
        pages.clear();
        startNewPage();

        traverse(root);
        return pages;
    }

    private void traverse(DocumentNode node) {
        node.accept(this);
        // Only traverse children if this is not a TableNode (it processes its children cells internally)
        if (!(node instanceof TableNode)) {
            for (DocumentNode child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    private void startNewPage() {
        currentPage = new Page(pages.size() + 1);
        pages.add(currentPage);
        currentY = marginTop;
    }

    private double getUsableWidth() {
        return pageWidth - marginLeft - marginRight;
    }

    private double getUsableHeight() {
        return pageHeight - marginTop - marginBottom;
    }

    @Override
    public void visit(ParagraphNode paragraph) {
        // Measure paragraph text wrap
        String text = paragraph.getText();
        double charWidth = 6.0; // Mock character width
        double lineHeight = 14.0;
        double usableWidth = getUsableWidth();

        int charsPerLine = (int) Math.max(1, usableWidth / charWidth);
        List<String> lines = wrapText(text, charsPerLine);

        for (String line : lines) {
            if (currentY + lineHeight > pageHeight - marginBottom) {
                startNewPage();
            }
            currentPage.addElement(new PlacedElement(
                paragraph,
                line,
                marginLeft,
                currentY,
                line.length() * charWidth,
                lineHeight
            ));
            currentY += lineHeight;
        }
        currentY += 8.0; // Paragraph spacing
    }

    @Override
    public void visit(ImageNode image) {
        double height = image.getHeight();
        if (currentY + height > pageHeight - marginBottom) {
            startNewPage();
        }
        currentPage.addElement(new PlacedElement(
            image,
            null,
            marginLeft,
            currentY,
            image.getWidth(),
            height
        ));
        currentY += height + 10.0; // Spacing
    }

    @Override
    public void visit(TableNode table) {
        // Table layout: measured rows and paginated row splits
        int rows = table.getRowCount();
        int cols = table.getColCount();
        double colWidth = getUsableWidth() / cols;
        double rowHeight = 24.0; // Fix height for table rows

        // Keep track of header cells to repeat on page breaks
        List<DocumentNode> headers = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            headers.add(table.getCell(0, c));
        }

        // Output header row first
        renderTableRow(table, 0, colWidth, rowHeight, headers);

        for (int r = 1; r < rows; r++) {
            if (currentY + rowHeight > pageHeight - marginBottom) {
                startNewPage();
                // Repeat headers on new page
                renderTableRow(table, 0, colWidth, rowHeight, headers);
            }
            renderTableRow(table, r, colWidth, rowHeight, null);
        }
        currentY += 12.0; // Spacing
    }

    private void renderTableRow(TableNode table, int rowIndex, double colWidth, double rowHeight, List<DocumentNode> optionalOverrideCells) {
        int cols = table.getColCount();
        for (int c = 0; c < cols; c++) {
            DocumentNode cell = (optionalOverrideCells != null) ? optionalOverrideCells.get(c) : table.getCell(rowIndex, c);
            if (cell != null) {
                // Placing the nested cell inside grid
                String cellText = "";
                if (cell instanceof ParagraphNode p) {
                    cellText = p.getText();
                }
                currentPage.addElement(new PlacedElement(
                    cell,
                    cellText,
                    marginLeft + (c * colWidth),
                    currentY,
                    colWidth,
                    rowHeight
                ));
            }
        }
        currentY += rowHeight;
    }

    @Override
    public void visit(ScriptNode script) {
        // Scripts do not render direct content except expression block mock
        double height = 15.0;
        if (currentY + height > pageHeight - marginBottom) {
            startNewPage();
        }
        currentPage.addElement(new PlacedElement(
            script,
            "Expression: " + script.getExpression(),
            marginLeft,
            currentY,
            getUsableWidth(),
            height
        ));
        currentY += height + 6.0;
    }

    private List<String> wrapText(String text, int limit) {
        List<String> list = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (sb.length() + word.length() + 1 > limit) {
                if (sb.length() > 0) {
                    list.add(sb.toString());
                    sb.setLength(0);
                }
                // Handle single very long word
                if (word.length() > limit) {
                    list.add(word.substring(0, limit));
                } else {
                    sb.append(word);
                }
            } else {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(word);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    @Override
    public List<Page> process(DocumentNode input) {
        return paginate(input);
    }
}

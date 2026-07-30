package com.aura.pipeline;

import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles a list of layout pages into a custom binary PDF-like mock vector stream.
 */
public class PdfMockRenderer implements TransformationPipeline.Stage<List<LayoutPaginator.Page>, byte[]> {

    public byte[] render(List<LayoutPaginator.Page> pages) throws IOException {
        Preconditions.checkNotNull(pages, "Pages list cannot be null");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 1. Write File Header
        baos.write("%PDF-MOCK-1.0\n".getBytes(StandardCharsets.US_ASCII));

        List<Integer> objectOffsets = new ArrayList<>();

        // 2. Write Page Objects
        for (int i = 0; i < pages.size(); i++) {
            objectOffsets.add(baos.size());
            LayoutPaginator.Page page = pages.get(i);
            
            String pageHeader = String.format("%d 0 obj\n<< /Type /Page /PageNumber %d >>\nstream\n",
                i + 1, page.getPageNumber());
            baos.write(pageHeader.getBytes(StandardCharsets.US_ASCII));

            for (LayoutPaginator.PlacedElement el : page.getElements()) {
                if (el.getRenderText() != null) {
                    // Text element: BT (Begin Text), Td (Offset), Tj (Show Text), ET (End Text)
                    String txtCmd = String.format("BT /F1 12 Tf %.2f %.2f Td (%s) Tj ET\n",
                        el.getX(), el.getY(), escapeText(el.getRenderText()));
                    baos.write(txtCmd.getBytes(StandardCharsets.UTF_8));
                } else {
                    // Image element
                    String imgCmd = String.format("BI /W %.2f /H %.2f %.2f %.2f Td EI\n",
                        el.getWidth(), el.getHeight(), el.getX(), el.getY());
                    baos.write(imgCmd.getBytes(StandardCharsets.US_ASCII));
                }
            }

            baos.write("endstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }

        // 3. Write Cross-Reference Table
        int xrefOffset = baos.size();
        baos.write("xref\n".getBytes(StandardCharsets.US_ASCII));
        String xrefHeader = String.format("0 %d\n", objectOffsets.size() + 1);
        baos.write(xrefHeader.getBytes(StandardCharsets.US_ASCII));
        baos.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));

        for (int offset : objectOffsets) {
            String entry = String.format("%010d 00000 n \n", offset);
            baos.write(entry.getBytes(StandardCharsets.US_ASCII));
        }

        // 4. Write Trailer
        baos.write("trailer\n<< /Size 0 >>\nstartxref\n".getBytes(StandardCharsets.US_ASCII));
        String startXref = String.format("%d\n", xrefOffset);
        baos.write(startXref.getBytes(StandardCharsets.US_ASCII));
        baos.write("%%EOF\n".getBytes(StandardCharsets.US_ASCII));

        return baos.toByteArray();
    }

    private String escapeText(String text) {
        if (text == null) return "";
        return text.replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("\\", "\\\\");
    }

    @Override
    public byte[] process(List<LayoutPaginator.Page> input) {
        try {
            return render(input);
        } catch (IOException e) {
            throw new RuntimeException("Mock PDF rendering failed", e);
        }
    }
}

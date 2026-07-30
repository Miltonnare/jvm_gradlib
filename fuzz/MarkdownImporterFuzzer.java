import com.aura.pipeline.MarkdownImporter;

public class MarkdownImporterFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length == 0) {
            return;
        }
        try {
            String md = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            MarkdownImporter importer = new MarkdownImporter();
            importer.importMarkdown(md);
        } catch (Exception e) {
            // Ignore expected Markdown parsing exceptions
        }
    }
}

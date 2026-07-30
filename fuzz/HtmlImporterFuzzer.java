import com.aura.pipeline.HtmlImporter;

public class HtmlImporterFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length == 0) {
            return;
        }
        try {
            String html = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            HtmlImporter importer = new HtmlImporter();
            importer.importHtml(html);
        } catch (Exception e) {
            // Ignore expected HTML parsing exceptions
        }
    }
}

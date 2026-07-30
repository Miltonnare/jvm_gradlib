import com.aura.pipeline.CssStyleParser;

public class CssStyleParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length == 0) {
            return;
        }
        try {
            String css = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            CssStyleParser parser = new CssStyleParser();
            parser.parse(css);
        } catch (Exception e) {
        }
    }
}

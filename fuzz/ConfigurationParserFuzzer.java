import com.aura.config.XonLexer;
import com.aura.config.XonParser;
import java.nio.charset.StandardCharsets;

public class ConfigurationParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length == 0) {
            return;
        }
        
        try {
            String src = new String(data, StandardCharsets.UTF_8);
            XonLexer lexer = new XonLexer(src);
            XonParser parser = new XonParser(lexer.tokenize());
            parser.parse();
        } catch (Exception e) {
            // Ignore syntax/lexer exceptions
        }
    }
}

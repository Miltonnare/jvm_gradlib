import com.aura.config.XonLexer;
import com.aura.config.XonObject;
import com.aura.config.XonParser;
import com.aura.model.DocumentGraph;
import com.aura.model.DocumentNode;
import com.aura.pipeline.FormatConverter;
import com.aura.pipeline.HtmlRenderer;
import com.aura.pipeline.MarkdownRenderer;
import com.aura.validation.ValidationEngine;
import java.nio.charset.StandardCharsets;

public class ObjectGraphFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length < 5) {
            return;
        }
        
        String src = new String(data, StandardCharsets.UTF_8);
        try {
            XonLexer lexer = new XonLexer(src);
            XonParser parser = new XonParser(lexer.tokenize());
            var val = parser.parse();
            if (val instanceof XonObject obj) {
                DocumentNode doc = FormatConverter.fromXon(obj);
                DocumentGraph graph = new DocumentGraph(doc);
                
                ValidationEngine engine = new ValidationEngine();
                engine.validate(graph);

                MarkdownRenderer mr = new MarkdownRenderer();
                mr.render(doc);

                HtmlRenderer hr = new HtmlRenderer();
                hr.render(doc);
            }
        } catch (Exception e) {
            // Ignore syntax, parser, validation and compilation exceptions
        }
    }
}

import com.aura.model.query.QueryExpression;

public class QueryParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length == 0) {
            return;
        }
        try {
            String expr = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            new QueryExpression(expr);
        } catch (Exception e) {
            // Ignore expected lexer/parser syntax errors
        }
    }
}

package com.aura.engine.template;

import com.aura.internal.Preconditions;
import java.util.List;
import java.util.Map;

/**
 * Main entry point for the AuraTemplate compiling and rendering engine.
 */
public class TemplateEngine {

    /**
     * Renders a template string using variable bindings.
     */
    public String render(String template, Map<String, Object> variables) {
        Preconditions.checkNotNull(template, "Template string cannot be null");
        Preconditions.checkNotNull(variables, "Variables map cannot be null");

        if (template.isEmpty()) {
            return "";
        }

        // 1. Lex
        TemplateLexer lexer = new TemplateLexer(template);
        List<TemplateToken> tokens = lexer.tokenize();

        // 2. Parse
        TemplateParser parser = new TemplateParser(tokens);
        TemplateAST.RootNode root = parser.parse();

        // 3. Render
        TemplateContext ctx = new TemplateContext(variables);
        StringBuilder sb = new StringBuilder();
        root.render(ctx, sb);

        return sb.toString();
    }
}

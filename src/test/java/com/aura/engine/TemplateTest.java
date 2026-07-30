package com.aura.engine;

import com.aura.engine.template.TemplateEngine;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateTest {

    @Test
    public void testTemplateRendering() {
        TemplateEngine engine = new TemplateEngine();
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("user", Map.of("name", "John", "active", true));
        
        List<String> items = new ArrayList<>();
        items.add("ItemA");
        items.add("ItemB");
        vars.put("items", items);

        String template = "Hello {{ user.name | upper }}!\n" +
                          "{{ if user.active }}\n" +
                          "Your loop:\n" +
                          "{{ for item in items }}\n" +
                          " - {{ item | lower }}\n" +
                          "{{ endfor }}\n" +
                          "{{ else }}\n" +
                          "Inactive account\n" +
                          "{{ endif }}";

        String rendered = engine.render(template, vars);
        
        assertTrue(rendered.contains("Hello JOHN!"));
        assertTrue(rendered.contains(" - itema"));
        assertTrue(rendered.contains(" - itemb"));
    }
}

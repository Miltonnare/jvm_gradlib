package com.aura.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineExtendedTest {

    @Test
    public void testHtmlRendererNull() {
        HtmlRenderer r = new HtmlRenderer();
        assertThrows(NullPointerException.class, () -> r.render(null));
    }
}

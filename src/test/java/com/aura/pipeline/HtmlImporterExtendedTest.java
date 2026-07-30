package com.aura.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HtmlImporterExtendedTest {

    @Test
    public void testHtmlImporterEmptyInput() {
        HtmlImporter importer = new HtmlImporter();
        assertThrows(IllegalArgumentException.class, () -> importer.importHtml(""));
    }
}

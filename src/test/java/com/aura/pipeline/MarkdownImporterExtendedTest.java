package com.aura.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownImporterExtendedTest {

    @Test
    public void testMarkdownImporterEmptyInput() {
        MarkdownImporter importer = new MarkdownImporter();
        assertThrows(IllegalArgumentException.class, () -> importer.importMarkdown(""));
    }
}

package com.aura.pipeline;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.encryption.XorEncryptionProvider;
import com.aura.config.XonObject;
import com.aura.container.AuraContainerReader;
import com.aura.container.AuraContainerWriter;
import com.aura.container.ContainerConstants;
import com.aura.metadata.MetadataValue;
import com.aura.model.DocumentNode;
import com.aura.model.ParagraphNode;
import com.aura.model.TableNode;
import com.aura.validation.ValidationEngine;
import com.aura.model.DocumentGraph;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineTest {

    @Test
    public void testTransformationPipeline() {
        TransformationPipeline<String, String> pipeline = TransformationPipeline.<String, String>builder()
            .addStage(String::toUpperCase)
            .addStage(s -> s + "!!!")
            .build();

        String result = pipeline.execute("hello");
        assertEquals("HELLO!!!", result);
    }

    @Test
    public void testFormatConverter() {
        ParagraphNode p = new ParagraphNode("intro", "This is the introduction.");
        p.setAttribute("class", "lead");

        XonObject xon = FormatConverter.toXon(p);
        assertTrue(xon.has("id"));
        assertEquals("intro", xon.get("id").asString());

        DocumentNode node = FormatConverter.fromXon(xon);
        assertEquals("intro", node.getId());
        assertEquals("paragraph", node.getNodeType());
        assertEquals("lead", node.getAttribute("class"));
    }

    @Test
    public void testDocumentRenderers() {
        TableNode root = new TableNode("root", 2, 2);
        root.setCell(0, 0, new ParagraphNode("h1", "Title"));
        root.setCell(0, 1, new ParagraphNode("h2", "Desc"));
        root.setCell(1, 0, new ParagraphNode("b1", "Content"));

        MarkdownRenderer md = new MarkdownRenderer();
        String markdown = md.render(root);
        assertTrue(markdown.contains("| Title | Desc |"));
        assertTrue(markdown.contains("| --- | --- |"));

        HtmlRenderer html = new HtmlRenderer();
        String markup = html.render(root);
        assertTrue(markup.contains("<table id=\"root\">"));
        assertTrue(markup.contains("<th>  <p id=\"h1\">Title</p>"));
        assertTrue(markup.contains("<td>  <p id=\"b1\">Content</p>"));
    }

    @Test
    public void testValidationEngine() {
        ParagraphNode p = new ParagraphNode("root", "Ok text");
        DocumentGraph g = new DocumentGraph(p);
        ValidationEngine engine = new ValidationEngine();
        assertDoesNotThrow(() -> engine.validate(g));
    }

    @Test
    public void testContainerIntegration() throws IOException {
        AuraContainerWriter writer = new AuraContainerWriter();
        XorEncryptionProvider enc = new XorEncryptionProvider(new byte[]{5, 6, 7});
        writer.setEncryptionProvider(enc);

        byte[] payload1 = "Container Resource Data Text content".getBytes();
        writer.addResource("docs/readme.txt", ContainerConstants.TYPE_TEXT, CompressionAlgorithm.LZ77, payload1);
        writer.setMetadata("version", MetadataValue.of(2));

        byte[] containerData = writer.build();
        assertNotNull(containerData);

        AuraContainerReader reader = new AuraContainerReader(containerData);
        reader.setEncryptionProvider(enc);

        assertEquals(2, reader.getMetadata().get("version").asInt());
        byte[] readBytes = reader.readResource("docs/readme.txt");
        assertArrayEquals(payload1, readBytes);
    }
}

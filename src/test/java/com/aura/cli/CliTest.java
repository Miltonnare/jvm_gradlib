package com.aura.cli;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class CliTest {

    @Test
    public void testCliCompressAndDecompress() throws IOException {
        File inputFile = File.createTempFile("cli-input", ".txt");
        File compFile = File.createTempFile("cli-comp", ".bin");
        File decompFile = File.createTempFile("cli-decomp", ".txt");
        inputFile.deleteOnExit();
        compFile.deleteOnExit();
        decompFile.deleteOnExit();

        String rawContent = "This is some test content to compress using the command line tool AuraCli! ".repeat(10);
        try (FileOutputStream fos = new FileOutputStream(inputFile)) {
            fos.write(rawContent.getBytes(StandardCharsets.UTF_8));
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        // 1. Run compress
        String[] compArgs = {
            "compress",
            "-i", inputFile.getAbsolutePath(),
            "-o", compFile.getAbsolutePath(),
            "-a", "LZ77"
        };
        int exitCode1 = AuraCli.run(compArgs, outStream, errStream);
        assertEquals(0, exitCode1, () -> "Compress failed: " + errStream.toString());
        assertTrue(compFile.length() > 0);

        // 2. Run decompress
        outStream.reset();
        errStream.reset();
        String[] decompArgs = {
            "decompress",
            "-i", compFile.getAbsolutePath(),
            "-o", decompFile.getAbsolutePath(),
            "-a", "LZ77"
        };
        int exitCode2 = AuraCli.run(decompArgs, outStream, errStream);
        assertEquals(0, exitCode2, () -> "Decompress failed: " + errStream.toString());

        byte[] decompBytes = new byte[(int) decompFile.length()];
        try (var fis = new java.io.FileInputStream(decompFile)) {
            fis.read(decompBytes);
        }
        assertEquals(rawContent, new String(decompBytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testCliOptionErrors() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        String[] badArgs = {"invalid-command"};
        int code = AuraCli.run(badArgs, outStream, errStream);
        assertEquals(1, code);
        assertTrue(errStream.toString().contains("Unknown command"));
    }
}

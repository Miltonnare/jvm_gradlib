package com.aura.cli;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class CliExtendedTest {

    @Test
    public void testCliMissingArgs() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        String[] compressArgs = {"compress"}; // missing input/output
        int code1 = AuraCli.run(compressArgs, outStream, errStream);
        assertEquals(1, code1);
        assertTrue(errStream.toString().contains("requires --input and --output"));

        outStream.reset();
        errStream.reset();
        String[] queryArgs = {"query"}; // missing query-str
        int code2 = AuraCli.run(queryArgs, outStream, errStream);
        assertEquals(1, code2);
        assertTrue(errStream.toString().contains("requires --input and --query-str"));
    }

    @Test
    public void testCliKeyringMissingArgs() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        String[] keyringArgs = {"keyring"}; // missing input
        int code = AuraCli.run(keyringArgs, outStream, errStream);
        assertEquals(1, code);
        assertTrue(errStream.toString().contains("requires --input"));
    }
}

package com.aura.cli;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.compression.CompressionProvider;
import com.aura.codec.encryption.AuraKeyRing;
import com.aura.config.XonParser;
import com.aura.config.XonValue;
import com.aura.internal.Preconditions;
import com.aura.model.DocumentNode;
import com.aura.model.query.QueryExpression;
import com.aura.pipeline.HtmlImporter;
import com.aura.pipeline.HtmlRenderer;
import com.aura.pipeline.MarkdownImporter;
import com.aura.pipeline.MarkdownRenderer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Command Line Interface for Aura Document Processing Engine.
 */
public class AuraCli {
    private static final String USAGE = 
        "Usage: AuraCli <command> [options]\n\n" +
        "Commands:\n" +
        "  compress    - Compress a file using LZ77 or RLE\n" +
        "  decompress  - Decompress a compressed file\n" +
        "  render-html - Render markdown/html source to HTML5 output\n" +
        "  render-md   - Render markdown/html source to GFM Markdown output\n" +
        "  query       - Query a document tree using path selectors\n" +
        "  keyring     - Create or print binary keyring status\n\n" +
        "Options:\n" +
        "  -i, --input <file>   - Path to input file\n" +
        "  -o, --output <file>  - Path to output file\n" +
        "  -a, --algo <algorithm> - NONE, RLE, or LZ77 (default: LZ77)\n" +
        "  -q, --query-str <str> - Path query expression\n";

    public static int run(String[] args, ByteArrayOutputStream outStream, ByteArrayOutputStream errStream) {
        Preconditions.checkNotNull(args, "Args cannot be null");
        if (args.length == 0) {
            write(errStream, USAGE);
            return 1;
        }

        String cmd = args[0];
        String inputPath = null;
        String outputPath = null;
        String algoStr = "LZ77";
        String queryStr = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                case "--input":
                    if (i + 1 < args.length) {
                        inputPath = args[++i];
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                    break;
                case "-a":
                case "--algo":
                    if (i + 1 < args.length) {
                        algoStr = args[++i];
                    }
                    break;
                case "-q":
                case "--query-str":
                    if (i + 1 < args.length) {
                        queryStr = args[++i];
                    }
                    break;
                default:
                    write(errStream, "Unknown option: " + args[i] + "\n" + USAGE);
                    return 1;
            }
        }

        try {
            switch (cmd) {
                case "compress" -> {
                    if (inputPath == null || outputPath == null) {
                        write(errStream, "Error: compress command requires --input and --output\n");
                        return 1;
                    }
                    byte[] raw = readFile(inputPath);
                    CompressionAlgorithm algo = CompressionAlgorithm.valueOf(algoStr.toUpperCase());
                    byte[] comp = CompressionProvider.compress(raw, algo);
                    writeFile(outputPath, comp);
                    write(outStream, "Successfully compressed " + raw.length + " bytes to " + comp.length + " bytes using " + algo + "\n");
                }
                case "decompress" -> {
                    if (inputPath == null || outputPath == null) {
                        write(errStream, "Error: decompress command requires --input and --output\n");
                        return 1;
                    }
                    byte[] comp = readFile(inputPath);
                    CompressionAlgorithm algo = CompressionAlgorithm.valueOf(algoStr.toUpperCase());
                    byte[] raw = CompressionProvider.decompress(comp, algo, 1024 * 1024);
                    writeFile(outputPath, raw);
                    write(outStream, "Successfully decompressed " + comp.length + " bytes to " + raw.length + " bytes\n");
                }
                case "render-html" -> {
                    if (inputPath == null || outputPath == null) {
                        write(errStream, "Error: render-html command requires --input and --output\n");
                        return 1;
                    }
                    String src = new String(readFile(inputPath), StandardCharsets.UTF_8);
                    DocumentNode doc = src.trim().startsWith("<") ? new HtmlImporter().importHtml(src) : new MarkdownImporter().importMarkdown(src);
                    String html = new HtmlRenderer().render(doc);
                    writeFile(outputPath, html.getBytes(StandardCharsets.UTF_8));
                    write(outStream, "Successfully rendered HTML to " + outputPath + "\n");
                }
                case "render-md" -> {
                    if (inputPath == null || outputPath == null) {
                        write(errStream, "Error: render-md command requires --input and --output\n");
                        return 1;
                    }
                    String src = new String(readFile(inputPath), StandardCharsets.UTF_8);
                    DocumentNode doc = src.trim().startsWith("<") ? new HtmlImporter().importHtml(src) : new MarkdownImporter().importMarkdown(src);
                    String mdOut = new MarkdownRenderer().render(doc);
                    writeFile(outputPath, mdOut.getBytes(StandardCharsets.UTF_8));
                    write(outStream, "Successfully rendered Markdown to " + outputPath + "\n");
                }
                case "query" -> {
                    if (inputPath == null || queryStr == null) {
                        write(errStream, "Error: query command requires --input and --query-str\n");
                        return 1;
                    }
                    String src = new String(readFile(inputPath), StandardCharsets.UTF_8);
                    DocumentNode doc = src.trim().startsWith("<") ? new HtmlImporter().importHtml(src) : new MarkdownImporter().importMarkdown(src);
                    QueryExpression query = new QueryExpression(queryStr);
                    List<DocumentNode> res = query.evaluate(doc);
                    write(outStream, "Query matched " + res.size() + " node(s):\n");
                    for (DocumentNode node : res) {
                        write(outStream, "  - [" + node.getNodeType() + "] ID: " + node.getId() + "\n");
                    }
                }
                case "keyring" -> {
                    if (inputPath == null) {
                        write(errStream, "Error: keyring command requires --input\n");
                        return 1;
                    }
                    byte[] bytes = readFile(inputPath);
                    AuraKeyRing ring = AuraKeyRing.parse(bytes);
                    write(outStream, "Keyring contains " + ring.getKeys().size() + " key(s):\n");
                    for (var entry : ring.getKeys().entrySet()) {
                        write(outStream, "  - Name: " + entry.getKey() + ", Algo: " + entry.getValue().getAlgorithm() + "\n");
                    }
                }
                default -> {
                    write(errStream, "Unknown command: " + cmd + "\n" + USAGE);
                    return 1;
                }
            }
        } catch (Exception e) {
            write(errStream, "Execution error: " + e.getMessage() + "\n");
            return 2;
        }

        return 0;
    }

    private static void write(ByteArrayOutputStream stream, String text) {
        try {
            stream.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {}
    }

    private static byte[] readFile(String path) throws IOException {
        File file = new File(path);
        byte[] buf = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buf);
        }
        return buf;
    }

    private static void writeFile(String path, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }
}

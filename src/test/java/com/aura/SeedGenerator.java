package com.aura;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.compression.CompressionProvider;
import com.aura.codec.compression.Lz77Compression;
import com.aura.codec.compression.RleCompression;
import com.aura.codec.encryption.AuraKeyRing;
import com.aura.codec.encryption.XorEncryptionProvider;
import com.aura.config.XonObject;
import com.aura.container.AuraContainerWriter;
import com.aura.container.ContainerConstants;
import com.aura.container.ContainerIndex;
import com.aura.container.ContainerIndexEntry;
import com.aura.metadata.BinaryMetadataWriter;
import com.aura.metadata.MetadataValue;
import com.aura.internal.ByteUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generator tool to create realistic seed corpus files for each fuzzer target.
 */
public class SeedGenerator {

    public static void main(String[] args) throws IOException {
        String baseDir = args.length > 0 ? args[0] : "fuzz/corpus";
        System.out.println("Generating seed corpus files in: " + baseDir);

        createDir(baseDir);

        generateContainerParserSeeds(baseDir + "/ContainerParserFuzzer");
        generateArchiveDecoderSeeds(baseDir + "/ArchiveDecoderFuzzer");
        generateMetadataParserSeeds(baseDir + "/MetadataParserFuzzer");
        generateObjectGraphSeeds(baseDir + "/ObjectGraphFuzzer");
        generateStreamReaderSeeds(baseDir + "/StreamReaderFuzzer");
        generateBinaryIndexSeeds(baseDir + "/BinaryIndexFuzzer");
        generateConfigurationParserSeeds(baseDir + "/ConfigurationParserFuzzer");
        generateEventReplaySeeds(baseDir + "/EventReplayFuzzer");
        generateQueryParserSeeds(baseDir + "/QueryParserFuzzer");
        generateKeyRingParserSeeds(baseDir + "/KeyRingParserFuzzer");
        generateHtmlImporterSeeds(baseDir + "/HtmlImporterFuzzer");
        generateMarkdownImporterSeeds(baseDir + "/MarkdownImporterFuzzer");
        generateCssStyleParserSeeds(baseDir + "/CssStyleParserFuzzer");

        System.out.println("Seed corpus generation complete.");
    }

    private static void createDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void writeFile(String path, byte[] data) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    private static void generateContainerParserSeeds(String path) throws IOException {
        AuraContainerWriter writer = new AuraContainerWriter();
        XorEncryptionProvider enc = new XorEncryptionProvider(new byte[]{0x55, (byte) 0xAA});
        writer.setEncryptionProvider(enc);

        writer.addResource("docs/manifest.xcf", ContainerConstants.TYPE_CONFIG, CompressionAlgorithm.LZ77,
            "{ version: 1, title: 'Seed container' }".getBytes());
        writer.addResource("assets/logo.png", ContainerConstants.TYPE_IMAGE, CompressionAlgorithm.RLE,
            new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4});

        writer.setMetadata("author", MetadataValue.of("Aura Fuzzer"));
        writer.setMetadata("build", MetadataValue.of(12345L));

        byte[] adc = writer.build();
        writeFile(path + "/valid_container.adc", adc);
    }

    private static void generateArchiveDecoderSeeds(String path) throws IOException {
        // LZ77 stream: start with type byte 1, then LZ77 compressed payload
        byte[] raw = "AAAAABBBBBCCCCCDDDDDthe quick brown fox jumps over the lazy dog. the quick brown fox jumps over the lazy dog.".getBytes();
        byte[] lz77 = Lz77Compression.compress(raw);
        byte[] lz77Seed = new byte[lz77.length + 1];
        lz77Seed[0] = 1; // LZ77 type indicator
        System.arraycopy(lz77, 0, lz77Seed, 1, lz77.length);
        writeFile(path + "/lz77_compressed.bin", lz77Seed);

        // RLE stream: start with type byte 0, then RLE compressed payload
        byte[] rle = RleCompression.compress(raw);
        byte[] rleSeed = new byte[rle.length + 1];
        rleSeed[0] = 0; // RLE type indicator
        System.arraycopy(rle, 0, rleSeed, 1, rle.length);
        writeFile(path + "/rle_compressed.bin", rleSeed);
    }

    private static void generateMetadataParserSeeds(String path) throws IOException {
        Map<String, MetadataValue> map = new LinkedHashMap<>();
        map.put("active", MetadataValue.of(true));
        map.put("ratio", MetadataValue.of(0.995));
        map.put("name", MetadataValue.of("Aura Metadata System"));
        
        List<MetadataValue> list = new ArrayList<>();
        list.add(MetadataValue.of(1));
        list.add(MetadataValue.of(2));
        map.put("indices", MetadataValue.of(list));

        byte[] bsm = BinaryMetadataWriter.write(map);
        writeFile(path + "/valid_metadata.bsm", bsm);
    }

    private static void generateObjectGraphSeeds(String path) throws IOException {
        String xcfDoc = """
            {
                id: "root"
                type: "document"
                attributes: {
                    title: "System Spec"
                    author: "Engineering"
                }
                children: [
                    {
                        id: "p1"
                        type: "paragraph"
                        text: "Introduction of layout flow"
                        attributes: { ref: "s1" }
                    }
                    {
                        id: "s1"
                        type: "script"
                        expression: "2 * count + 1"
                    }
                ]
            }
            """;
        writeFile(path + "/document_graph.xcf", xcfDoc.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateStreamReaderSeeds(String path) throws IOException {
        // Stream format: limit byte followed by data
        byte[] seed = new byte[20];
        seed[0] = 10; // limit reading to 10 bytes
        for (int i = 1; i < 20; i++) {
            seed[i] = (byte) i;
        }
        writeFile(path + "/stream_bytes.bin", seed);
    }

    private static void generateBinaryIndexSeeds(String path) throws IOException {
        List<ContainerIndexEntry> entries = new ArrayList<>();
        entries.add(new ContainerIndexEntry(UUID.randomUUID(), ContainerConstants.TYPE_TEXT, (byte) 0, 100L, 500L, 500L, "text.txt", 0xABCDEF12L));
        entries.add(new ContainerIndexEntry(UUID.randomUUID(), ContainerConstants.TYPE_IMAGE, (byte) 2, 600L, 1000L, 800L, "img.png", 0x78901234L));
        ContainerIndex index = new ContainerIndex(entries);
        writeFile(path + "/index_block.bin", index.serialize());
    }

    private static void generateConfigurationParserSeeds(String path) throws IOException {
        String config = """
            # Server Config
            server {
                host: "127.0.0.1"
                port: 9000
                ssl: false
                ciphers: ["TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256"]
            }
            """;
        writeFile(path + "/server_config.xcf", config.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateEventReplaySeeds(String path) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] countBytes = new byte[4];
        ByteUtils.writeIntBe(countBytes, 0, 2); // 2 events
        baos.write(countBytes);

        // Event 1: "start"
        byte[] timeBytes = new byte[8];
        ByteUtils.writeLongBe(timeBytes, 0, 1000L);
        baos.write(timeBytes);
        byte[] nameBytes = "start".getBytes(StandardCharsets.UTF_8);
        byte[] nameLenBuf = new byte[2];
        ByteUtils.writeShortBe(nameLenBuf, 0, (short) nameBytes.length);
        baos.write(nameLenBuf);
        baos.write(nameBytes);
        byte[] mutCount = new byte[2];
        ByteUtils.writeShortBe(mutCount, 0, (short) 0);
        baos.write(mutCount);

        // Event 2: "stop" with speed context mutation
        ByteUtils.writeLongBe(timeBytes, 0, 5000L);
        baos.write(timeBytes);
        byte[] nameBytes2 = "stop".getBytes(StandardCharsets.UTF_8);
        ByteUtils.writeShortBe(nameLenBuf, 0, (short) nameBytes2.length);
        baos.write(nameLenBuf);
        baos.write(nameBytes2);
        ByteUtils.writeShortBe(mutCount, 0, (short) 1);
        baos.write(mutCount);

        // Context mutation: key "speed", type tag 1 (int), value 0
        byte[] keyBytes = "speed".getBytes(StandardCharsets.UTF_8);
        byte[] keyLen = new byte[2];
        ByteUtils.writeShortBe(keyLen, 0, (short) keyBytes.length);
        baos.write(keyLen);
        baos.write(keyBytes);
        baos.write(1); // type tag 1
        byte[] valLen = new byte[4];
        ByteUtils.writeIntBe(valLen, 0, 4);
        baos.write(valLen);
        byte[] valBytes = new byte[4];
        ByteUtils.writeIntBe(valBytes, 0, 0); // speed = 0
        baos.write(valBytes);

        writeFile(path + "/events.log", baos.toByteArray());
    }

    private static void generateQueryParserSeeds(String path) throws IOException {
        String query = "//paragraph[@class='body'][1]";
        writeFile(path + "/query.txt", query.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateKeyRingParserSeeds(String path) throws IOException {
        AuraKeyRing keyring = new AuraKeyRing();
        keyring.addKey("seed-xor", (byte) 1, new byte[]{0x12, 0x34}, null);
        keyring.addKey("seed-aes", (byte) 2, new byte[16], new byte[16]);
        writeFile(path + "/keyring.bin", keyring.serialize());
    }

    private static void generateHtmlImporterSeeds(String path) throws IOException {
        String html = "<article id=\"root\"><p id=\"p1\">Test Paragraph</p></article>";
        writeFile(path + "/test.html", html.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateMarkdownImporterSeeds(String path) throws IOException {
        String md = "This is seed paragraph.\n\n| H1 | H2 |\n| --- | --- |\n| C1 | C2 |";
        writeFile(path + "/test.md", md.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateCssStyleParserSeeds(String path) throws IOException {
        String css = "#intro {\n  color: red;\n  font-size: 14px;\n}";
        writeFile(path + "/test.css", css.getBytes(StandardCharsets.UTF_8));
    }
}

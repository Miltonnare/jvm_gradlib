package com.aura.metadata;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes Binary Structured Metadata (BSM) from streams with nesting limits.
 */
public final class BinaryMetadataReader {
    private static final int MAX_DEPTH = 16;
    private static final int MAX_KEY_SIZE = 1024;
    private static final int MAX_STRING_SIZE = 65536;
    private static final int MAX_BINARY_SIZE = 1048576; // 1MB limit
    private static final int MAX_ARRAY_COUNT = 4096;

    private BinaryMetadataReader() {
        // Prevent instantiation
    }

    public static Map<String, MetadataValue> read(byte[] data) throws IOException {
        Preconditions.checkNotNull(data, "Data cannot be null");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return readMap(in, 0);
    }

    private static Map<String, MetadataValue> readMap(InputStream in, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException("Maximum metadata parsing depth exceeded");
        }

        Map<String, MetadataValue> result = new LinkedHashMap<>();
        while (true) {
            int tag = in.read();
            if (tag == -1) {
                break; // EOF of this map block
            }

            MetadataType type = MetadataType.fromCode((byte) tag);

            // Read Key Length (2 bytes)
            byte[] keyLenBuf = new byte[2];
            int read = readFully(in, keyLenBuf);
            if (read == -1) {
                throw new IllegalArgumentException("Truncated metadata: key length expected");
            }
            int keyLen = ByteUtils.readShortBe(keyLenBuf, 0) & 0xFFFF;
            if (keyLen > MAX_KEY_SIZE) {
                throw new IllegalArgumentException("Metadata key size exceeds limit of " + MAX_KEY_SIZE);
            }

            // Read Key bytes
            byte[] keyBytes = new byte[keyLen];
            if (readFully(in, keyBytes) < keyLen) {
                throw new IllegalArgumentException("Truncated metadata: key bytes expected");
            }
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            // Read Value Length (4 bytes)
            byte[] valLenBuf = new byte[4];
            if (readFully(in, valLenBuf) < 4) {
                throw new IllegalArgumentException("Truncated metadata: value length expected");
            }
            int valLen = ByteUtils.readIntBe(valLenBuf, 0);
            if (valLen < 0 || valLen > MAX_BINARY_SIZE) {
                throw new IllegalArgumentException("Metadata value length is invalid or exceeds limit: " + valLen);
            }

            // Read Value bytes
            byte[] valBytes = new byte[valLen];
            if (readFully(in, valBytes) < valLen) {
                throw new IllegalArgumentException("Truncated metadata: value bytes expected");
            }

            MetadataValue value = parseValue(type, valBytes, depth + 1);
            result.put(key, value);
        }
        return result;
    }

    private static MetadataValue parseValue(MetadataType type, byte[] data, int depth) throws IOException {
        switch (type) {
            case INTEGER -> {
                if (data.length != 4) {
                    throw new IllegalArgumentException("Invalid byte length for integer: " + data.length);
                }
                return MetadataValue.of(ByteUtils.readIntBe(data, 0));
            }
            case LONG -> {
                if (data.length != 8) {
                    throw new IllegalArgumentException("Invalid byte length for long: " + data.length);
                }
                return MetadataValue.of(ByteUtils.readLongBe(data, 0));
            }
            case DOUBLE -> {
                if (data.length != 8) {
                    throw new IllegalArgumentException("Invalid byte length for double: " + data.length);
                }
                long bits = ByteUtils.readLongBe(data, 0);
                return MetadataValue.of(Double.longBitsToDouble(bits));
            }
            case BOOLEAN -> {
                if (data.length != 1) {
                    throw new IllegalArgumentException("Invalid byte length for boolean: " + data.length);
                }
                return MetadataValue.of(data[0] != 0);
            }
            case STRING -> {
                if (data.length > MAX_STRING_SIZE) {
                    throw new IllegalArgumentException("String value exceeds limit of " + MAX_STRING_SIZE);
                }
                return MetadataValue.of(new String(data, StandardCharsets.UTF_8));
            }
            case BINARY -> {
                if (data.length > MAX_BINARY_SIZE) {
                    throw new IllegalArgumentException("Binary size exceeds limit of " + MAX_BINARY_SIZE);
                }
                return MetadataValue.of(data);
            }
            case MAP -> {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                return MetadataValue.of(readMap(bais, depth));
            }
            case ARRAY -> {
                if (data.length < 4) {
                    throw new IllegalArgumentException("Truncated array content: expected count");
                }
                int count = ByteUtils.readIntBe(data, 0);
                if (count < 0 || count > MAX_ARRAY_COUNT) {
                    throw new IllegalArgumentException("Array count exceeds limit: " + count);
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(data, 4, data.length - 4);
                List<MetadataValue> list = new ArrayList<>(Math.min(count, 128)); // Cap initial capacity
                for (int i = 0; i < count; i++) {
                    int elTag = bais.read();
                    if (elTag == -1) {
                        throw new IllegalArgumentException("Truncated array at element: " + i);
                    }
                    MetadataType elType = MetadataType.fromCode((byte) elTag);

                    byte[] elLenBuf = new byte[4];
                    if (bais.read(elLenBuf) < 4) {
                        throw new IllegalArgumentException("Truncated array element length at index: " + i);
                    }
                    int elLen = ByteUtils.readIntBe(elLenBuf, 0);
                    if (elLen < 0 || elLen > MAX_BINARY_SIZE) {
                        throw new IllegalArgumentException("Array element size invalid at index: " + i);
                    }

                    byte[] elBytes = new byte[elLen];
                    if (bais.read(elBytes) < elLen) {
                        throw new IllegalArgumentException("Truncated array element value at index: " + i);
                    }

                    list.add(parseValue(elType, elBytes, depth + 1));
                }
                return MetadataValue.of(list);
            }
        }
        throw new IllegalArgumentException("Unsupported metadata type: " + type);
    }

    private static int readFully(InputStream in, byte[] b) throws IOException {
        int total = 0;
        while (total < b.length) {
            int result = in.read(b, total, b.length - total);
            if (result == -1) {
                return total == 0 ? -1 : total;
            }
            total += result;
        }
        return total;
    }
}

package com.aura.metadata;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Serializes maps of MetadataValue entries to Binary Structured Metadata (BSM).
 */
public final class BinaryMetadataWriter {

    private BinaryMetadataWriter() {
        // Prevent instantiation
    }

    public static byte[] write(Map<String, MetadataValue> metadata) throws IOException {
        Preconditions.checkNotNull(metadata, "Metadata map cannot be null");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeMap(out, metadata);
        return out.toByteArray();
    }

    private static void writeMap(OutputStream out, Map<String, MetadataValue> map) throws IOException {
        for (Map.Entry<String, MetadataValue> entry : map.entrySet()) {
            String key = entry.getKey();
            MetadataValue value = entry.getValue();

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Preconditions.checkArgument(keyBytes.length <= 0xFFFF, "Key length exceeds maximum of 65535 bytes");

            // 1. Tag (Type)
            out.write(value.getType().getCode());

            // 2. Key Length (2 bytes)
            byte[] keyLenBuf = new byte[2];
            ByteUtils.writeShortBe(keyLenBuf, 0, (short) keyBytes.length);
            out.write(keyLenBuf);

            // 3. Key bytes
            out.write(keyBytes);

            // Serialize value into buffer to know the length first
            byte[] valBytes = serializeValue(value);

            // 4. Value Length (4 bytes)
            byte[] valLenBuf = new byte[4];
            ByteUtils.writeIntBe(valLenBuf, 0, valBytes.length);
            out.write(valLenBuf);

            // 5. Value bytes
            out.write(valBytes);
        }
    }

    private static byte[] serializeValue(MetadataValue value) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        switch (value.getType()) {
            case INTEGER -> {
                byte[] b = new byte[4];
                ByteUtils.writeIntBe(b, 0, value.asInt());
                buffer.write(b);
            }
            case LONG -> {
                byte[] b = new byte[8];
                ByteUtils.writeLongBe(b, 0, value.asLong());
                buffer.write(b);
            }
            case DOUBLE -> {
                byte[] b = new byte[8];
                long bits = Double.doubleToLongBits(value.asDouble());
                ByteUtils.writeLongBe(b, 0, bits);
                buffer.write(b);
            }
            case BOOLEAN -> buffer.write(value.asBoolean() ? 1 : 0);
            case STRING -> {
                byte[] sBytes = value.asString().getBytes(StandardCharsets.UTF_8);
                buffer.write(sBytes);
            }
            case BINARY -> buffer.write(value.asBinary());
            case MAP -> writeMap(buffer, value.asMap());
            case ARRAY -> {
                List<MetadataValue> list = value.asArray();
                byte[] countBuf = new byte[4];
                ByteUtils.writeIntBe(countBuf, 0, list.size());
                buffer.write(countBuf);
                for (MetadataValue val : list) {
                    buffer.write(val.getType().getCode());
                    byte[] elementBytes = serializeValue(val);
                    byte[] elLenBuf = new byte[4];
                    ByteUtils.writeIntBe(elLenBuf, 0, elementBytes.length);
                    buffer.write(elLenBuf);
                    buffer.write(elementBytes);
                }
            }
        }
        return buffer.toByteArray();
    }
}

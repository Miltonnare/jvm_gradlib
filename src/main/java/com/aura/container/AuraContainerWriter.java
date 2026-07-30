package com.aura.container;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.compression.CompressionProvider;
import com.aura.codec.encryption.EncryptionProvider;
import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import com.aura.metadata.BinaryMetadataWriter;
import com.aura.metadata.MetadataValue;
import com.aura.stream.SeekingBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Writer for generating Aura Document Containers (.adc).
 */
public class AuraContainerWriter {
    private final Map<String, PendingResource> resources = new LinkedHashMap<>();
    private final Map<String, MetadataValue> metadata = new LinkedHashMap<>();
    private EncryptionProvider encryptionProvider;
    private short flags = 0;

    public static final class PendingResource {
        private final UUID id;
        private final byte fileType;
        private final CompressionAlgorithm compression;
        private final byte[] data;

        public PendingResource(UUID id, byte fileType, CompressionAlgorithm compression, byte[] data) {
            this.id = id;
            this.fileType = fileType;
            this.compression = compression;
            this.data = data;
        }
    }

    public void setEncryptionProvider(EncryptionProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
        if (encryptionProvider != null) {
            this.flags |= 0x01; // Enable Encryption Flag
        } else {
            this.flags &= ~0x01;
        }
    }

    public void addResource(String name, byte fileType, CompressionAlgorithm compression, byte[] data) {
        Preconditions.checkNotNull(name, "Resource name cannot be null");
        Preconditions.checkNotNull(data, "Resource data cannot be null");
        Preconditions.checkArgument(!resources.containsKey(name), "Resource already exists: " + name);
        resources.put(name, new PendingResource(UUID.randomUUID(), fileType, compression, data));
    }

    public void setMetadata(String key, MetadataValue val) {
        Preconditions.checkNotNull(key, "Metadata key cannot be null");
        Preconditions.checkNotNull(val, "Metadata value cannot be null");
        metadata.put(key, val);
    }

    public byte[] build() throws IOException {
        SeekingBuffer buffer = new SeekingBuffer();

        // 1. Allocate space for header (written as dummy first, rewritten at the end)
        byte[] dummyHeader = new byte[ContainerConstants.HEADER_SIZE];
        buffer.write(dummyHeader, 0, dummyHeader.length);

        List<ContainerIndexEntry> indexEntries = new ArrayList<>();

        // 2. Write Resource blocks
        for (Map.Entry<String, PendingResource> entry : resources.entrySet()) {
            String name = entry.getKey();
            PendingResource res = entry.getValue();

            // Compress payload
            byte[] processed = CompressionProvider.compress(res.data, res.compression);
            long uncompressedLen = res.data.length;

            // Encrypt if encryption is configured
            if (encryptionProvider != null) {
                processed = encryptionProvider.encrypt(processed);
            }

            long offset = buffer.position();
            long compressedLen = processed.length;

            // Compute payload checksum (on the written raw bytes)
            long checksum = Crc32.compute(processed);

            // Write payload to buffer
            buffer.write(processed, 0, processed.length);

            // Keep track of index entry
            ContainerIndexEntry indexEntry = new ContainerIndexEntry(
                res.id,
                res.fileType,
                res.compression.getCode(),
                offset,
                uncompressedLen,
                compressedLen,
                name,
                checksum
            );
            indexEntries.add(indexEntry);
        }

        // 3. Write Metadata block
        long metadataOffset = buffer.position();
        byte[] metadataBytes = BinaryMetadataWriter.write(metadata);
        buffer.write(metadataBytes, 0, metadataBytes.length);

        // 4. Write Index block
        long indexOffset = buffer.position();
        ContainerIndex indexBlock = new ContainerIndex(indexEntries);
        byte[] indexBytes = indexBlock.serialize();
        buffer.write(indexBytes, 0, indexBytes.length);

        // 5. Seek back to 0 and write the finalized Header
        ContainerHeader finalHeader = new ContainerHeader(
            (byte) 1, // Major version
            (byte) 0, // Minor version
            flags,
            metadataOffset,
            indexOffset,
            0 // Checksum will be computed inside serialize()
        );

        buffer.seek(0);
        byte[] serializedHeader = finalHeader.serialize();
        buffer.write(serializedHeader, 0, serializedHeader.length);

        return buffer.getBytes();
    }
}

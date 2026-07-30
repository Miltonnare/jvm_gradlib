package com.aura.container;

import com.aura.codec.compression.CompressionAlgorithm;
import com.aura.codec.compression.CompressionProvider;
import com.aura.codec.encryption.EncryptionProvider;
import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import com.aura.metadata.BinaryMetadataReader;
import com.aura.metadata.MetadataValue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Reader for the Aura Document Container format (.adc).
 */
public class AuraContainerReader {
    private static final int MAX_UNCOMPRESSED_PAYLOAD = 16 * 1024 * 1024; // 16MB limit per entry to prevent OOM
    private final byte[] fileData;
    private ContainerHeader header;
    private ContainerIndex index;
    private Map<String, MetadataValue> metadata;
    private EncryptionProvider encryptionProvider;

    public AuraContainerReader(byte[] fileData) {
        this.fileData = Preconditions.checkNotNull(fileData, "File data cannot be null").clone();
        parseContainer();
    }

    public void setEncryptionProvider(EncryptionProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }

    private void parseContainer() {
        if (fileData.length < ContainerConstants.HEADER_SIZE) {
            throw new ContainerException("Container data is smaller than minimum header size");
        }

        // 1. Parse Header
        byte[] headerBytes = Arrays.copyOfRange(fileData, 0, ContainerConstants.HEADER_SIZE);
        this.header = ContainerHeader.parse(headerBytes);

        // 2. Validate Offsets
        long metadataOffset = header.getMetadataOffset();
        long indexOffset = header.getIndexOffset();

        if (metadataOffset >= fileData.length || indexOffset >= fileData.length) {
            throw new ContainerException("Header offsets point beyond file size");
        }

        // 3. Parse Index Block (runs to EOF or next block)
        // Find index length
        int indexLen = (int) (fileData.length - indexOffset);
        if (metadataOffset > indexOffset) {
            indexLen = (int) (metadataOffset - indexOffset);
        }
        if (indexLen <= 0) {
            throw new ContainerException("Invalid index block length calculation");
        }

        byte[] indexData = new byte[indexLen];
        System.arraycopy(fileData, (int) indexOffset, indexData, 0, indexLen);
        this.index = ContainerIndex.parse(indexData);

        // 4. Parse Metadata Block
        int metadataLen = (int) (fileData.length - metadataOffset);
        if (indexOffset > metadataOffset) {
            metadataLen = (int) (indexOffset - metadataOffset);
        }
        if (metadataLen <= 0) {
            throw new ContainerException("Invalid metadata block length calculation");
        }

        byte[] metadataData = new byte[metadataLen];
        System.arraycopy(fileData, (int) metadataOffset, metadataData, 0, metadataLen);
        try {
            this.metadata = BinaryMetadataReader.read(metadataData);
        } catch (IOException e) {
            throw new ContainerException("Failed to decode container metadata block", e);
        }
    }

    /**
     * Reads a resource payload by name, verifying integrity and applying de-processing (decryption/decompression).
     */
    public byte[] readResource(String name) {
        Preconditions.checkNotNull(name, "Resource name cannot be null");
        ContainerIndexEntry entry = index.findEntry(name);
        if (entry == null) {
            throw new ContainerException("Resource not found in container: " + name);
        }

        long offset = entry.getOffset();
        long compressedLen = entry.getCompressedLength();
        long uncompressedLen = entry.getLength();

        if (offset < ContainerConstants.HEADER_SIZE || offset + compressedLen > fileData.length) {
            throw new ContainerException(String.format("Resource offsets point outside file bounds: offset=%d, size=%d, fileLen=%d",
                offset, compressedLen, fileData.length));
        }

        byte[] rawPayload = new byte[(int) compressedLen];
        System.arraycopy(fileData, (int) offset, rawPayload, 0, (int) compressedLen);

        // 1. Verify CRC32 checksum of the raw payload (as stored)
        long computedChecksum = Crc32.compute(rawPayload);
        if (computedChecksum != entry.getPayloadChecksum()) {
            throw new ContainerException(String.format("Integrity failure for resource '%s': expected checksum 0x%X, got 0x%X",
                name, entry.getPayloadChecksum(), computedChecksum));
        }

        // 2. Decrypt if container flags or compression layout dictates
        byte[] processedData = rawPayload;
        boolean encrypted = (header.getFlags() & 0x01) != 0;
        if (encrypted) {
            if (encryptionProvider == null) {
                throw new ContainerException("Resource is encrypted but no EncryptionProvider is configured");
            }
            processedData = encryptionProvider.decrypt(processedData);
        }

        // 3. Decompress if compression is applied
        byte compCode = entry.getCompressionType();
        CompressionAlgorithm algo = CompressionAlgorithm.fromCode(compCode);
        if (algo != CompressionAlgorithm.NONE) {
            int safetyLimit = (int) Math.min(MAX_UNCOMPRESSED_PAYLOAD, uncompressedLen * 2 + 1024);
            processedData = CompressionProvider.decompress(processedData, algo, safetyLimit);
        }

        // 4. Verify uncompressed length matches expected
        if (processedData.length != uncompressedLen) {
            throw new ContainerException(String.format("Resource size mismatch after decompression: expected %d bytes, got %d bytes",
                uncompressedLen, processedData.length));
        }

        return processedData;
    }

    public ContainerHeader getHeader() { return header; }
    public ContainerIndex getIndex() { return index; }
    public Map<String, MetadataValue> getMetadata() { return metadata; }
}

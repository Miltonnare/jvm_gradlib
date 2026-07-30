package com.aura.container;

import com.aura.internal.ByteUtils;
import com.aura.internal.Crc32;
import com.aura.internal.Preconditions;
import java.util.Arrays;

/**
 * Representation and verification of the binary Aura Container Header.
 */
public class ContainerHeader {
    private final byte majorVersion;
    private final byte minorVersion;
    private final short flags;
    private final long metadataOffset;
    private final long indexOffset;
    private final long headerChecksum;

    public ContainerHeader(byte majorVersion, byte minorVersion, short flags, long metadataOffset, long indexOffset, long headerChecksum) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.flags = flags;
        this.metadataOffset = metadataOffset;
        this.indexOffset = indexOffset;
        this.headerChecksum = headerChecksum;
    }

    public static ContainerHeader parse(byte[] headerData) {
        Preconditions.checkNotNull(headerData, "Header data cannot be null");
        if (headerData.length < ContainerConstants.HEADER_SIZE) {
            throw new ContainerException("Header data is truncated");
        }

        // 1. Verify Magic
        byte[] magic = Arrays.copyOfRange(headerData, 0, 4);
        if (!Arrays.equals(magic, ContainerConstants.MAGIC)) {
            throw new ContainerException("Invalid container magic signature: " + ByteUtils.toHexString(magic));
        }

        // 2. Verify Checksum of the header block
        long expectedChecksum = ByteUtils.readIntBe(headerData, 24) & 0xFFFFFFFFL;
        long computedChecksum = Crc32.compute(headerData, 0, 24);
        if (expectedChecksum != computedChecksum) {
            throw new ContainerException(String.format("Header checksum verification failed: expected 0x%X but computed 0x%X",
                expectedChecksum, computedChecksum));
        }

        byte major = headerData[4];
        byte minor = headerData[5];
        if (major != 1) {
            throw new ContainerException("Unsupported container major version: " + major);
        }

        short flags = ByteUtils.readShortBe(headerData, 6);
        long metadataOffset = ByteUtils.readLongBe(headerData, 8);
        long indexOffset = ByteUtils.readLongBe(headerData, 16);

        if (metadataOffset < ContainerConstants.HEADER_SIZE || indexOffset < ContainerConstants.HEADER_SIZE) {
            throw new ContainerException(String.format("Invalid offset in header: metadataOffset=%d, indexOffset=%d",
                metadataOffset, indexOffset));
        }

        return new ContainerHeader(major, minor, flags, metadataOffset, indexOffset, expectedChecksum);
    }

    public byte[] serialize() {
        byte[] data = new byte[ContainerConstants.HEADER_SIZE];
        System.arraycopy(ContainerConstants.MAGIC, 0, data, 0, 4);
        data[4] = majorVersion;
        data[5] = minorVersion;
        ByteUtils.writeShortBe(data, 6, flags);
        ByteUtils.writeLongBe(data, 8, metadataOffset);
        ByteUtils.writeLongBe(data, 16, indexOffset);

        // Compute and write checksum
        long crc = Crc32.compute(data, 0, 24);
        ByteUtils.writeIntBe(data, 24, (int) crc);

        return data;
    }

    public byte getMajorVersion() { return majorVersion; }
    public byte getMinorVersion() { return minorVersion; }
    public short getFlags() { return flags; }
    public long getMetadataOffset() { return metadataOffset; }
    public long getIndexOffset() { return indexOffset; }
    public long getHeaderChecksum() { return headerChecksum; }
}

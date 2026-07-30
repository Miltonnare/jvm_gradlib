package com.aura.container;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Representation of a resource entry inside the container index.
 */
public class ContainerIndexEntry {
    private final UUID id;
    private final byte fileType;
    private final byte compressionType;
    private final long offset;
    private final long length;
    private final long compressedLength;
    private final String name;
    private final long payloadChecksum;

    public ContainerIndexEntry(UUID id, byte fileType, byte compressionType, long offset, long length, long compressedLength, String name, long payloadChecksum) {
        this.id = Preconditions.checkNotNull(id, "ID cannot be null");
        this.fileType = fileType;
        this.compressionType = compressionType;
        this.offset = offset;
        this.length = length;
        this.compressedLength = compressedLength;
        this.name = Preconditions.checkNotNull(name, "Name cannot be null");
        this.payloadChecksum = payloadChecksum;
    }

    public static ContainerIndexEntry deserialize(byte[] data, int startOffset) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        Preconditions.checkPositionIndexes(startOffset, startOffset + 48, data.length);

        // 1. UUID (16 bytes)
        long mostSig = ByteUtils.readLongBe(data, startOffset);
        long leastSig = ByteUtils.readLongBe(data, startOffset + 8);
        UUID id = new UUID(mostSig, leastSig);

        // 2. File type & compression type (2 bytes)
        byte fileType = data[startOffset + 16];
        byte compressionType = data[startOffset + 17];

        // 3. Offsets and sizes (24 bytes)
        long offset = ByteUtils.readLongBe(data, startOffset + 18);
        long length = ByteUtils.readLongBe(data, startOffset + 26);
        long compressedLength = ByteUtils.readLongBe(data, startOffset + 34);

        // 4. Checksum (4 bytes)
        long payloadChecksum = ByteUtils.readIntBe(data, startOffset + 42) & 0xFFFFFFFFL;

        // 5. Name length (2 bytes)
        int nameLen = ByteUtils.readShortBe(data, startOffset + 46) & 0xFFFF;
        Preconditions.checkPositionIndexes(startOffset + 48, startOffset + 48 + nameLen, data.length);

        // 6. Name
        String name = new String(data, startOffset + 48, nameLen, StandardCharsets.UTF_8);

        return new ContainerIndexEntry(id, fileType, compressionType, offset, length, compressedLength, name, payloadChecksum);
    }

    public byte[] serialize() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        Preconditions.checkArgument(nameBytes.length <= 0xFFFF, "Name too long, exceeds 65535 bytes");

        byte[] entryBytes = new byte[48 + nameBytes.length];

        // 1. UUID (16 bytes)
        ByteUtils.writeLongBe(entryBytes, 0, id.getMostSignificantBits());
        ByteUtils.writeLongBe(entryBytes, 8, id.getLeastSignificantBits());

        // 2. Types
        entryBytes[16] = fileType;
        entryBytes[17] = compressionType;

        // 3. Offsets/Lengths
        ByteUtils.writeLongBe(entryBytes, 18, offset);
        ByteUtils.writeLongBe(entryBytes, 26, length);
        ByteUtils.writeLongBe(entryBytes, 34, compressedLength);

        // 4. Checksum
        ByteUtils.writeIntBe(entryBytes, 42, (int) payloadChecksum);

        // 5. Name length (2 bytes)
        ByteUtils.writeShortBe(entryBytes, 46, (short) nameBytes.length);

        // 6. Name
        System.arraycopy(nameBytes, 0, entryBytes, 48, nameBytes.length);

        return entryBytes;
    }

    public UUID getId() { return id; }
    public byte getFileType() { return fileType; }
    public byte getCompressionType() { return compressionType; }
    public long getOffset() { return offset; }
    public long getLength() { return length; }
    public long getCompressedLength() { return compressedLength; }
    public String getName() { return name; }
    public long getPayloadChecksum() { return payloadChecksum; }
}

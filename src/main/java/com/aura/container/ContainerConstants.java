package com.aura.container;

/**
 * Constants for the Aura Document Container format.
 */
public final class ContainerConstants {
    private ContainerConstants() {
        // Prevent instantiation
    }

    /**
     * Magic signature bytes: 'A' 'U' 'R' 'A'
     */
    public static final byte[] MAGIC = {(byte) 'A', (byte) 'U', (byte) 'R', (byte) 'A'};

    public static final int HEADER_SIZE = 28; // 4 (magic) + 2 (version) + 2 (flags) + 8 (metadata offset) + 8 (index offset) + 4 (checksum)
    
    // File Type codes
    public static final byte TYPE_TEXT = 0x01;
    public static final byte TYPE_IMAGE = 0x02;
    public static final byte TYPE_CONFIG = 0x03;
    public static final byte TYPE_BINARY = 0x04;
    public static final byte TYPE_COMPRESSED_STREAM = 0x05;
}

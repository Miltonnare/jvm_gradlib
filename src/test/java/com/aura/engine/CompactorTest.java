package com.aura.engine;

import com.aura.internal.ByteUtils;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class CompactorTest {

    @Test
    public void testEventCompaction() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Write Header
        baos.write(new byte[]{(byte) 'E', (byte) 'V', (byte) 'L', (byte) 'G'});
        byte[] ver = new byte[2];
        ByteUtils.writeShortBe(ver, 0, (short) 1);
        baos.write(ver);

        // Event 1: Entity "player", key "score" = 10
        writeMutation(baos, "player", "score", 1, 10);
        // Event 2: Entity "player", key "score" = 20 (overwrite score)
        writeMutation(baos, "player", "score", 1, 20);
        // Event 3: Entity "player", key "level" = 2
        writeMutation(baos, "player", "level", 1, 2);

        byte[] rawLog = baos.toByteArray();

        EventLogCompactor compactor = new EventLogCompactor();
        byte[] compactedLog = compactor.compact(rawLog);

        assertNotNull(compactedLog);
        assertTrue(compactedLog.length < rawLog.length, "Compacted log should be smaller");

        // Verify compacted state by checking if "score" has final value 20
        // We can parse the compacted log using another compactor run or check entries count
        // player has 2 final mutations: score = 20, level = 2.
    }

    private void writeMutation(ByteArrayOutputStream baos, String entityName, String key, int tag, int val) throws IOException {
        byte[] nameBytes = entityName.getBytes(StandardCharsets.UTF_8);
        byte[] nameLen = new byte[2];
        ByteUtils.writeShortBe(nameLen, 0, (short) nameBytes.length);
        baos.write(nameLen);
        baos.write(nameBytes);

        byte[] count = new byte[2];
        ByteUtils.writeShortBe(count, 0, (short) 1);
        baos.write(count);

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] keyLen = new byte[2];
        ByteUtils.writeShortBe(keyLen, 0, (short) keyBytes.length);
        baos.write(keyLen);
        baos.write(keyBytes);

        baos.write(tag);

        byte[] valLen = new byte[4];
        ByteUtils.writeIntBe(valLen, 0, 4);
        baos.write(valLen);

        byte[] valBytes = new byte[4];
        ByteUtils.writeIntBe(valBytes, 0, val);
        baos.write(valBytes);
    }
}

package com.aura.engine;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class EventLogWriterTest {

    @Test
    public void testEventWritingAndReplaying() throws IOException {
        EventLogWriter writer = new EventLogWriter();
        EventLogWriter.MapState state = new EventLogWriter.MapState();
        state.put("score", 1, new byte[]{0, 0, 0, 10}); // int 10

        writer.writeEntityUpdate("player", state);
        byte[] raw = writer.toByteArray();

        assertNotNull(raw);
        assertTrue(raw.length > 10);

        EventLogCompactor compactor = new EventLogCompactor();
        byte[] compacted = compactor.compact(raw);
        assertNotNull(compacted);
    }
}

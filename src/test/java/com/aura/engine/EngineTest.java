package com.aura.engine;

import com.aura.internal.ByteUtils;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class EngineTest {

    @Test
    public void testStateMachine() {
        StateMachine fsm = new StateMachine("DRAFT");
        fsm.addTransition("DRAFT", new StateMachine.Transition("review", "IN_REVIEW", null, null));
        fsm.addTransition("IN_REVIEW", new StateMachine.Transition("approve", "APPROVED", "score", "90"));
        fsm.addTransition("IN_REVIEW", new StateMachine.Transition("reject", "DRAFT", null, null));

        assertTrue(fsm.triggerEvent("review"));
        assertEquals("IN_REVIEW", fsm.getCurrentState());

        // Attempt approve without context variable -> fails guard
        assertFalse(fsm.triggerEvent("approve"));
        assertEquals("IN_REVIEW", fsm.getCurrentState());

        // Add score -> passes guard
        fsm.setContextValue("score", 90);
        assertTrue(fsm.triggerEvent("approve"));
        assertEquals("APPROVED", fsm.getCurrentState());
    }

    @Test
    public void testEventReplayEngine() throws IOException {
        StateMachine fsm = new StateMachine("INIT");
        fsm.addTransition("INIT", new StateMachine.Transition("start", "RUNNING", null, null));
        fsm.addTransition("RUNNING", new StateMachine.Transition("stop", "STOPPED", "speed", "0"));

        EventReplayEngine engine = new EventReplayEngine(fsm);

        // Serialize a binary event stream
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

        byte[] mutationCountBuf = new byte[2];
        ByteUtils.writeShortBe(mutationCountBuf, 0, (short) 0); // 0 context mutations
        baos.write(mutationCountBuf);

        // Event 2: "stop" with mutation "speed" = 0
        ByteUtils.writeLongBe(timeBytes, 0, 2000L);
        baos.write(timeBytes);
        
        byte[] nameBytes2 = "stop".getBytes(StandardCharsets.UTF_8);
        ByteUtils.writeShortBe(nameLenBuf, 0, (short) nameBytes2.length);
        baos.write(nameLenBuf);
        baos.write(nameBytes2);

        ByteUtils.writeShortBe(mutationCountBuf, 0, (short) 1); // 1 context mutation
        baos.write(mutationCountBuf);

        // Mutation: key "speed", tag 1 (int), value 0
        byte[] keyBytes = "speed".getBytes(StandardCharsets.UTF_8);
        byte[] keyLenBuf = new byte[2];
        ByteUtils.writeShortBe(keyLenBuf, 0, (short) keyBytes.length);
        baos.write(keyLenBuf);
        baos.write(keyBytes);

        baos.write(1); // tag 1 (int)
        byte[] valLenBuf = new byte[4];
        ByteUtils.writeIntBe(valLenBuf, 0, 4); // value size = 4
        baos.write(valLenBuf);
        byte[] valBytes = new byte[4];
        ByteUtils.writeIntBe(valBytes, 0, 0); // speed = 0
        baos.write(valBytes);

        int processed = engine.replay(baos.toByteArray());
        assertEquals(2, processed);
        assertEquals("STOPPED", fsm.getCurrentState());
    }

    @Test
    public void testTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        String temp = "Hello {{user}}, status: {{if vip}}VIP{{endif}} items: {{each items}}- {{name}} ({{val}}){{endeach}}";

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("user", "Admin");
        ctx.put("vip", true);

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "Docs");
        item1.put("val", 5);
        items.add(item1);
        ctx.put("items", items);

        String result = engine.render(temp, ctx);
        assertEquals("Hello Admin, status: VIP items: - Docs (5)", result.trim());
    }
}

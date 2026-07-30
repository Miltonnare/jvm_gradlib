package com.aura.engine;

import com.aura.internal.ByteUtils;
import com.aura.internal.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decodes a binary event log stream and feeds events into a StateMachine.
 */
public class EventReplayEngine {
    private static final int MAX_EVENTS = 10000;
    private static final int MAX_MUTATIONS = 100;
    private static final int MAX_STRING_LEN = 1024;
    private static final int MAX_VALUE_LEN = 65536;

    private final StateMachine stateMachine;

    public EventReplayEngine(StateMachine stateMachine) {
        this.stateMachine = Preconditions.checkNotNull(stateMachine, "StateMachine cannot be null");
    }

    /**
     * Replays the binary event log data into the state machine.
     * Returns the count of successfully processed events.
     */
    public int replay(byte[] binaryEvents) throws IOException {
        Preconditions.checkNotNull(binaryEvents, "Events data cannot be null");
        ByteArrayInputStream in = new ByteArrayInputStream(binaryEvents);
        
        byte[] countBuf = new byte[4];
        if (readFully(in, countBuf) < 4) {
            return 0; // Empty or truncated stream
        }
        int eventCount = ByteUtils.readIntBe(countBuf, 0);
        if (eventCount < 0 || eventCount > MAX_EVENTS) {
            throw new IllegalArgumentException("Invalid event count: " + eventCount);
        }

        int processed = 0;
        for (int i = 0; i < eventCount; i++) {
            // Read event time offset (8 bytes, long)
            byte[] timeBuf = new byte[8];
            if (readFully(in, timeBuf) < 8) {
                throw new IllegalArgumentException("Truncated event log: expected timestamp for event " + i);
            }
            long timestamp = ByteUtils.readLongBe(timeBuf, 0);

            // Read Event name (2 bytes length + string)
            byte[] nameLenBuf = new byte[2];
            if (readFully(in, nameLenBuf) < 2) {
                throw new IllegalArgumentException("Truncated event log: expected event name length for event " + i);
            }
            int nameLen = ByteUtils.readShortBe(nameLenBuf, 0) & 0xFFFF;
            if (nameLen > MAX_STRING_LEN) {
                throw new IllegalArgumentException("Event name length exceeds limit: " + nameLen);
            }
            byte[] nameBytes = new byte[nameLen];
            if (readFully(in, nameBytes) < nameLen) {
                throw new IllegalArgumentException("Truncated event log: expected event name bytes for event " + i);
            }
            String eventName = new String(nameBytes, StandardCharsets.UTF_8);

            // Read context mutations (2 bytes count)
            byte[] mutationsCountBuf = new byte[2];
            if (readFully(in, mutationsCountBuf) < 2) {
                throw new IllegalArgumentException("Truncated event log: expected mutation count for event " + i);
            }
            int mutationCount = ByteUtils.readShortBe(mutationsCountBuf, 0) & 0xFFFF;
            if (mutationCount > MAX_MUTATIONS) {
                throw new IllegalArgumentException("Mutation count exceeds safety limit: " + mutationCount);
            }

            // Apply mutations
            for (int m = 0; m < mutationCount; m++) {
                // Key length (2 bytes)
                byte[] keyLenBuf = new byte[2];
                if (readFully(in, keyLenBuf) < 2) {
                    throw new IllegalArgumentException("Truncated mutation key length");
                }
                int keyLen = ByteUtils.readShortBe(keyLenBuf, 0) & 0xFFFF;
                if (keyLen > MAX_STRING_LEN) {
                    throw new IllegalArgumentException("Mutation key length exceeds limit");
                }
                byte[] keyBytes = new byte[keyLen];
                if (readFully(in, keyBytes) < keyLen) {
                    throw new IllegalArgumentException("Truncated mutation key");
                }
                String key = new String(keyBytes, StandardCharsets.UTF_8);

                // Type (1 byte)
                int typeTag = in.read();
                if (typeTag == -1) {
                    throw new IllegalArgumentException("Truncated mutation type tag");
                }

                // Value length (4 bytes)
                byte[] valLenBuf = new byte[4];
                if (readFully(in, valLenBuf) < 4) {
                    throw new IllegalArgumentException("Truncated mutation value length");
                }
                int valLen = ByteUtils.readIntBe(valLenBuf, 0);
                if (valLen < 0 || valLen > MAX_VALUE_LEN) {
                    throw new IllegalArgumentException("Mutation value size invalid: " + valLen);
                }

                byte[] valBytes = new byte[valLen];
                if (readFully(in, valBytes) < valLen) {
                    throw new IllegalArgumentException("Truncated mutation value bytes");
                }

                Object mutatedVal = parseMutationValue((byte) typeTag, valBytes);
                stateMachine.setContextValue(key, mutatedVal);
            }

            // Trigger FSM transition
            boolean transitionSucceeded = stateMachine.triggerEvent(eventName);
            if (!transitionSucceeded) {
                // We could throw an exception or handle silently.
                // Throwing an exception is perfect for fuzzing execution paths!
                throw new IllegalStateException(String.format("Failed to transition state on event '%s' at timestamp %d. Current State: %s",
                    eventName, timestamp, stateMachine.getCurrentState()));
            }
            processed++;
        }

        return processed;
    }

    private Object parseMutationValue(byte tag, byte[] data) {
        return switch (tag) {
            case 1 -> {
                if (data.length != 4) throw new IllegalArgumentException("Int value must be 4 bytes");
                yield ByteUtils.readIntBe(data, 0);
            }
            case 2 -> {
                if (data.length != 8) throw new IllegalArgumentException("Long value must be 8 bytes");
                yield ByteUtils.readLongBe(data, 0);
            }
            case 3 -> {
                if (data.length != 1) throw new IllegalArgumentException("Boolean value must be 1 byte");
                yield data[0] != 0;
            }
            case 4 -> new String(data, StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException("Unknown context type tag: " + tag);
        };
    }

    private int readFully(InputStream in, byte[] b) throws IOException {
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

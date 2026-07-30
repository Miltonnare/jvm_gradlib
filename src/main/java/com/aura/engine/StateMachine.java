package com.aura.engine;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A finite state machine (FSM) loaded from configuration rules.
 */
public class StateMachine {
    private final String initialState;
    private final Map<String, List<Transition>> transitions = new HashMap<>();
    private String currentState;
    private final Map<String, Object> context = new HashMap<>();

    public static final class Transition {
        private final String event;
        private final String targetState;
        private final String guardKey;
        private final String guardExpectedValue;

        public Transition(String event, String targetState, String guardKey, String guardExpectedValue) {
            this.event = Preconditions.checkNotNull(event, "Event cannot be null");
            this.targetState = Preconditions.checkNotNull(targetState, "Target state cannot be null");
            this.guardKey = guardKey;
            this.guardExpectedValue = guardExpectedValue;
        }

        public String getEvent() { return event; }
        public String getTargetState() { return targetState; }
        public String getGuardKey() { return guardKey; }
        public String getGuardExpectedValue() { return guardExpectedValue; }

        public boolean evaluateGuard(Map<String, Object> ctx) {
            if (guardKey == null) {
                return true; // No guard, always passes
            }
            Object val = ctx.get(guardKey);
            if (val == null) {
                return guardExpectedValue == null || "null".equals(guardExpectedValue);
            }
            return val.toString().equals(guardExpectedValue);
        }
    }

    public StateMachine(String initialState) {
        this.initialState = Preconditions.checkNotNull(initialState, "Initial state cannot be null");
        this.currentState = initialState;
    }

    public void addTransition(String sourceState, Transition trans) {
        Preconditions.checkNotNull(sourceState, "Source state cannot be null");
        Preconditions.checkNotNull(trans, "Transition cannot be null");
        transitions.computeIfAbsent(sourceState, k -> new ArrayList<>()).add(trans);
    }

    public void setContextValue(String key, Object val) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        context.put(key, val);
    }

    public Object getContextValue(String key) {
        return context.get(key);
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    public String getCurrentState() {
        return currentState;
    }

    /**
     * Attempts to transition the FSM with the given event trigger.
     * Returns true if transition succeeded, false otherwise.
     */
    public boolean triggerEvent(String event) {
        Preconditions.checkNotNull(event, "Event trigger cannot be null");
        List<Transition> sourceTrans = transitions.get(currentState);
        if (sourceTrans == null) {
            return false;
        }

        for (Transition trans : sourceTrans) {
            if (trans.getEvent().equalsIgnoreCase(event)) {
                if (trans.evaluateGuard(context)) {
                    currentState = trans.getTargetState();
                    return true;
                }
            }
        }
        return false;
    }

    public void reset() {
        this.currentState = initialState;
        this.context.clear();
    }
}

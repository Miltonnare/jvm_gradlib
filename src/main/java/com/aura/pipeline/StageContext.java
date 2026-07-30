package com.aura.pipeline;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks execution logs, performance timings, and warnings across conversion pipeline stages.
 */
public class StageContext {
    private final Map<String, Object> attributes = new HashMap<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, Long> performanceTimings = new HashMap<>();

    public void setAttribute(String key, Object value) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return attributes.get(key);
    }

    public void addWarning(String warning) {
        Preconditions.checkNotNull(warning, "Warning cannot be null");
        warnings.add(warning);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void recordTiming(String stageName, long elapsedMs) {
        Preconditions.checkNotNull(stageName, "Stage name cannot be null");
        Preconditions.checkArgument(elapsedMs >= 0, "Timing offset must be non-negative");
        performanceTimings.put(stageName, elapsedMs);
    }

    public Map<String, Long> getPerformanceTimings() {
        return Collections.unmodifiableMap(performanceTimings);
    }

    public void clear() {
        attributes.clear();
        warnings.clear();
        performanceTimings.clear();
    }
}

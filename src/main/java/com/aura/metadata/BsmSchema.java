package com.aura.metadata;

import com.aura.internal.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata schema defining constraint rules for BSM TLV records maps.
 */
public class BsmSchema {
    private final Map<String, BsmFieldConstraint> constraints = new HashMap<>();

    public void addConstraint(BsmFieldConstraint constraint) {
        Preconditions.checkNotNull(constraint, "Constraint cannot be null");
        constraints.put(constraint.getKeyName(), constraint);
    }

    /**
     * Validates a map of metadata properties against schema constraints.
     */
    public void validate(Map<String, MetadataValue> metadata) {
        Preconditions.checkNotNull(metadata, "Metadata map cannot be null");

        for (Map.Entry<String, MetadataValue> entry : metadata.entrySet()) {
            BsmFieldConstraint constraint = constraints.get(entry.getKey());
            if (constraint != null) {
                constraint.validate(entry.getValue());
            }
        }

        // Verify that all required fields are present
        for (Map.Entry<String, BsmFieldConstraint> entry : constraints.entrySet()) {
            if (entry.getValue().isRequired() && !metadata.containsKey(entry.getKey())) {
                throw new IllegalArgumentException(String.format("Missing required metadata key: %s",
                    entry.getKey()));
            }
        }
    }
}

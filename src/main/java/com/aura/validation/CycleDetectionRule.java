package com.aura.validation;

import com.aura.model.DocumentGraph;

/**
 * Validation rule checking for cyclic cross-references in the document model.
 */
public class CycleDetectionRule implements ValidationRule {

    @Override
    public void validate(DocumentGraph graph) throws ValidationException {
        if (graph.hasCircularReferences()) {
            throw new ValidationException("Cycle detected in document cross-references");
        }
    }

    @Override
    public String getName() {
        return "CycleDetection";
    }
}

package com.aura.validation;

import com.aura.model.DocumentGraph;

/**
 * Interface defining a validation rule on a Document Graph.
 */
public interface ValidationRule {

    /**
     * Executes the validation rule.
     * Throws a ValidationException if verification fails.
     */
    void validate(DocumentGraph graph) throws ValidationException;

    /**
     * Unique name of the validation rule.
     */
    String getName();
}

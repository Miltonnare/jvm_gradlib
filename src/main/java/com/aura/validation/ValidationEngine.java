package com.aura.validation;

import com.aura.internal.Preconditions;
import com.aura.model.DocumentGraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Engine that runs multiple validation rules sequentially on a document graph.
 */
public class ValidationEngine {
    private final List<ValidationRule> rules = new ArrayList<>();

    public ValidationEngine() {
        // Register default rules
        rules.add(new CycleDetectionRule());
        rules.add(new IntegrityRule());
        rules.add(new SchemaComplianceRule());
    }

    public void registerRule(ValidationRule rule) {
        Preconditions.checkNotNull(rule, "Rule cannot be null");
        rules.add(rule);
    }

    public List<ValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Executes all rules. Throws ValidationException on the first violation.
     */
    public void validate(DocumentGraph graph) throws ValidationException {
        Preconditions.checkNotNull(graph, "Document graph cannot be null");
        for (ValidationRule rule : rules) {
            rule.validate(graph);
        }
    }
}

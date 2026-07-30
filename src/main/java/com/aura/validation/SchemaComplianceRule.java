package com.aura.validation;

import com.aura.model.DocumentGraph;
import com.aura.model.DocumentNode;

/**
 * Validation rule checking that required common fields and structures conform to standard schemas.
 */
public class SchemaComplianceRule implements ValidationRule {

    @Override
    public void validate(DocumentGraph graph) throws ValidationException {
        DocumentNode root = graph.getRoot();
        if (root == null) {
            throw new ValidationException("Document graph has no root node");
        }
        
        // Root node must be a container-like element or have an ID matching root schema
        if (!"root".equalsIgnoreCase(root.getId()) && !"document".equalsIgnoreCase(root.getNodeType())) {
            throw new ValidationException("Root node ID must be 'root' or type 'document'");
        }

        // Validate all nodes have non-null types
        for (DocumentNode node : graph.getNodeIndex().values()) {
            if (node.getNodeType() == null || node.getNodeType().trim().isEmpty()) {
                throw new ValidationException("Node type cannot be null or empty for node ID: " + node.getId());
            }
        }
    }

    @Override
    public String getName() {
        return "SchemaCompliance";
    }
}

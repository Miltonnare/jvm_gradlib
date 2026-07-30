package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.List;
import java.util.Map;

/**
 * Validates a parsed XonObject configuration against constraints.
 */
public class XonSchemaValidator {

    /**
     * Exception thrown when validation fails.
     */
    public static class SchemaException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SchemaException(String message) {
            super(message);
        }

        public SchemaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final XonObject schema;

    public XonSchemaValidator(XonObject schema) {
        this.schema = Preconditions.checkNotNull(schema, "Schema object cannot be null");
    }

    /**
     * Validates the target config against the schema rules.
     */
    public void validate(XonObject config) {
        Preconditions.checkNotNull(config, "Config cannot be null");
        validateObject(config, schema, "$");
    }

    private void validateObject(XonObject obj, XonObject ruleObj, String path) {
        Map<String, XonValue> objFields = obj.asObject();
        Map<String, XonValue> ruleFields = ruleObj.asObject();

        for (Map.Entry<String, XonValue> entry : ruleFields.entrySet()) {
            String field = entry.getKey();
            XonValue ruleSpec = entry.getValue();

            if (!ruleSpec.isObject()) {
                throw new SchemaException(String.format("Invalid schema layout: rule for field '%s' must be an object spec", field));
            }
            XonObject spec = (XonObject) ruleSpec;
            String fieldPath = path + "." + field;

            boolean required = false;
            if (spec.has("required") && spec.get("required").isPrimitive()) {
                required = spec.get("required").asBoolean();
            }

            if (!obj.has(field)) {
                if (required) {
                    throw new SchemaException(String.format("Missing required field '%s' at %s", field, path));
                }
                continue; // Field is optional and missing, skip validation
            }

            XonValue targetVal = obj.get(field);
            validateField(targetVal, spec, fieldPath);
        }
    }

    private void validateField(XonValue val, XonObject spec, String path) {
        String expectedType = "";
        if (spec.has("type") && spec.get("type").isPrimitive()) {
            expectedType = spec.get("type").asString();
        }

        // Validate types
        switch (expectedType.toLowerCase()) {
            case "string" -> {
                if (!val.isPrimitive() || val.isNull()) {
                    throw new SchemaException(String.format("Field %s must be a string, got %s", path, val));
                }
                validateString(val.asString(), spec, path);
            }
            case "number" -> {
                if (!val.isPrimitive() || val.isNull() || val.toString().startsWith("\"")) {
                    // String check wrapper
                    throw new SchemaException(String.format("Field %s must be a number, got %s", path, val));
                }
                try {
                    validateNumber(val.asDouble(), spec, path);
                } catch (UnsupportedOperationException e) {
                    throw new SchemaException(String.format("Field %s must be a number, got %s", path, val), e);
                }
            }
            case "boolean" -> {
                if (!val.isPrimitive() || val.isNull()) {
                    throw new SchemaException(String.format("Field %s must be a boolean, got %s", path, val));
                }
            }
            case "object" -> {
                if (!val.isObject()) {
                    throw new SchemaException(String.format("Field %s must be an object, got %s", path, val));
                }
                if (spec.has("properties") && spec.get("properties").isObject()) {
                    validateObject((XonObject) val, (XonObject) spec.get("properties"), path);
                }
            }
            case "array" -> {
                if (!val.isArray()) {
                    throw new SchemaException(String.format("Field %s must be an array, got %s", path, val));
                }
                validateArray((XonArray) val, spec, path);
            }
            default -> {
                // If no type specified, we skip type check but check values if needed
            }
        }
    }

    private void validateString(String s, XonObject spec, String path) {
        if (spec.has("minLength") && spec.get("minLength").isPrimitive()) {
            int min = spec.get("minLength").asInt();
            if (s.length() < min) {
                throw new SchemaException(String.format("String field %s length %d is less than minLength %d", path, s.length(), min));
            }
        }
        if (spec.has("maxLength") && spec.get("maxLength").isPrimitive()) {
            int max = spec.get("maxLength").asInt();
            if (s.length() > max) {
                throw new SchemaException(String.format("String field %s length %d is greater than maxLength %d", path, s.length(), max));
            }
        }
        if (spec.has("pattern") && spec.get("pattern").isPrimitive()) {
            String pattern = spec.get("pattern").asString();
            if (!s.matches(pattern)) {
                throw new SchemaException(String.format("String field %s does not match pattern '%s'", path, pattern));
            }
        }
    }

    private void validateNumber(double num, XonObject spec, String path) {
        if (spec.has("minimum") && spec.get("minimum").isPrimitive()) {
            double min = spec.get("minimum").asDouble();
            if (num < min) {
                throw new SchemaException(String.format("Number field %s value %f is less than minimum %f", path, num, min));
            }
        }
        if (spec.has("maximum") && spec.get("maximum").isPrimitive()) {
            double max = spec.get("maximum").asDouble();
            if (num > max) {
                throw new SchemaException(String.format("Number field %s value %f is greater than maximum %f", path, num, max));
            }
        }
    }

    private void validateArray(XonArray arr, XonObject spec, String path) {
        List<XonValue> list = arr.asArray();
        if (spec.has("minItems") && spec.get("minItems").isPrimitive()) {
            int min = spec.get("minItems").asInt();
            if (list.size() < min) {
                throw new SchemaException(String.format("Array field %s size %d is less than minItems %d", path, list.size(), min));
            }
        }
        if (spec.has("maxItems") && spec.get("maxItems").isPrimitive()) {
            int max = spec.get("maxItems").asInt();
            if (list.size() > max) {
                throw new SchemaException(String.format("Array field %s size %d is greater than maxItems %d", path, list.size(), max));
            }
        }

        if (spec.has("items") && spec.get("items").isObject()) {
            XonObject itemSpec = (XonObject) spec.get("items");
            for (int i = 0; i < list.size(); i++) {
                validateField(list.get(i), itemSpec, path + "[" + i + "]");
            }
        }
    }
}

package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a schema description from Xon configuration format
 * and performs validation checks on configuration values.
 */
public class XonSchemaCompiler {

    public static class CompiledSchema {
        private String type;
        private boolean required;
        private Double minimum;
        private Double maximum;
        private List<String> enumValues;
        private Map<String, CompiledSchema> properties = new HashMap<>();
        private Set<String> requiredFields = new HashSet<>();
        private CompiledSchema itemsSchema;

        public void validate(XonValue val) {
            Preconditions.checkNotNull(val, "Configuration value cannot be null");
            
            if (type != null) {
                checkType(val);
            }

            if (enumValues != null && !enumValues.isEmpty()) {
                checkEnum(val);
            }

            if (val.isObject()) {
                validateObject((XonObject) val);
            } else if (val.isArray()) {
                validateArray((XonArray) val);
            } else if (val.isInt() || val.isDouble()) {
                validateNumeric(val);
            }
        }

        private void checkType(XonValue val) {
            boolean valid = switch (type.toLowerCase()) {
                case "object" -> val.isObject();
                case "array" -> val.isArray();
                case "string" -> val.isString();
                case "int" -> val.isInt();
                case "double" -> val.isDouble() || val.isInt();
                case "boolean" -> val.isBoolean();
                case "null" -> val.isNull();
                default -> true;
            };
            if (!valid) {
                throw new IllegalArgumentException(String.format("Xon Schema type mismatch: expected %s, got value node type", type));
            }
        }

        private void checkEnum(XonValue val) {
            if (!val.isString()) {
                throw new IllegalArgumentException("Enum constraint only supports string configuration nodes");
            }
            String strVal = val.asString();
            if (!enumValues.contains(strVal)) {
                throw new IllegalArgumentException(String.format("Xon value '%s' is not in the allowed schema enum: %s",
                    strVal, enumValues));
            }
        }

        private void validateObject(XonObject obj) {
            Map<String, XonValue> fields = obj.asObject();

            // Verify required fields
            for (String req : requiredFields) {
                if (!fields.containsKey(req)) {
                    throw new IllegalArgumentException("Missing required configuration field: " + req);
                }
            }

            // Verify properties constraints recursively
            for (Map.Entry<String, XonValue> entry : fields.entrySet()) {
                CompiledSchema propSchema = properties.get(entry.getKey());
                if (propSchema != null) {
                    propSchema.validate(entry.getValue());
                }
            }
        }

        private void validateArray(XonArray array) {
            if (itemsSchema != null) {
                for (XonValue item : array.asArray()) {
                    itemsSchema.validate(item);
                }
            }
        }

        private void validateNumeric(XonValue val) {
            double dVal = val.isInt() ? val.asInt() : val.asDouble();
            if (minimum != null && dVal < minimum) {
                throw new IllegalArgumentException(String.format("Xon value %f is below schema minimum %f",
                    dVal, minimum));
            }
            if (maximum != null && dVal > maximum) {
                throw new IllegalArgumentException(String.format("Xon value %f exceeds schema maximum %f",
                    dVal, maximum));
            }
        }
    }

    public CompiledSchema compile(String schemaSource) {
        Preconditions.checkNotNull(schemaSource, "Schema source string cannot be null");
        XonLexer lexer = new XonLexer(schemaSource);
        XonParser parser = new XonParser(lexer.tokenize());
        XonValue parsed = parser.parse();
        
        if (!parsed.isObject()) {
            throw new IllegalArgumentException("Xon Schema description must be an object structure");
        }

        return compileSchema((XonObject) parsed);
    }

    private CompiledSchema compileSchema(XonObject obj) {
        CompiledSchema compiled = new CompiledSchema();
        Map<String, XonValue> fields = obj.asObject();

        if (fields.containsKey("type")) {
            compiled.type = fields.get("type").asString();
        }
        if (fields.containsKey("required")) {
            compiled.required = fields.get("required").asBoolean();
        }
        if (fields.containsKey("minimum")) {
            compiled.minimum = fields.get("minimum").isDouble() ? fields.get("minimum").asDouble() : (double) fields.get("minimum").asInt();
        }
        if (fields.containsKey("maximum")) {
            compiled.maximum = fields.get("maximum").isDouble() ? fields.get("maximum").asDouble() : (double) fields.get("maximum").asInt();
        }
        
        if (fields.containsKey("enum")) {
            compiled.enumValues = new ArrayList<>();
            for (XonValue val : ((XonArray) fields.get("enum")).asArray()) {
                compiled.enumValues.add(val.asString());
            }
        }

        if (fields.containsKey("properties")) {
            XonObject props = (XonObject) fields.get("properties");
            for (Map.Entry<String, XonValue> entry : props.asObject().entrySet()) {
                compiled.properties.put(entry.getKey(), compileSchema((XonObject) entry.getValue()));
            }
        }

        if (fields.containsKey("requiredFields")) {
            for (XonValue req : ((XonArray) fields.get("requiredFields")).asArray()) {
                compiled.requiredFields.add(req.asString());
            }
        }

        if (fields.containsKey("items")) {
            compiled.itemsSchema = compileSchema((XonObject) fields.get("items"));
        }

        return compiled;
    }
}
